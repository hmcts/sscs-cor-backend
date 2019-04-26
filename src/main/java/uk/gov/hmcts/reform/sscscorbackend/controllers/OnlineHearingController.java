package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.CohEventActionMapper;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;


@Slf4j
@RestController
public class OnlineHearingController {

    private final OnlineHearingService onlineHearingService;
    private final CohEventActionMapper cohEventActionMapper;


    public OnlineHearingController(@Autowired OnlineHearingService onlineHearingService,
                                   @Autowired CohEventActionMapper cohEventActionMapper) {
        this.onlineHearingService = onlineHearingService;
        this.cohEventActionMapper = cohEventActionMapper;
    }

    @ApiOperation(value = "Create online hearing",
            notes = "Creates an online hearing in COH API. This will get triggered by a CDM event."
    )
    @RequestMapping(value = "/notify/onlineappeal", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> catchEvent(@RequestBody CcdEvent request) {
        if (request == null
            || request.getCaseDetails() == null
            || request.getCaseDetails().getCaseId() == null) {
            //throw a bad request error
            return ResponseEntity.badRequest().build();
        }

        boolean onlineHearingCreated = onlineHearingService.createOnlineHearing(request);

        return ResponseEntity.ok("{ \"onlineHearingCreated\": " + onlineHearingCreated + " }");
    }

    @ApiOperation(value = "Handle COH events",
            notes = "Currently we need to handle two types of events. The " +
                    "continuous_online_hearing_relisted to get the details of " +
                    "the online hearing, e.g. the questions and answers, write " +
                    "them into a pdf and attach the pdf to the case in CCD. The " +
                    "decision_issued to store the tribunals view as a pdf in CCD " +
                    "then send an email to the appellant."
    )
    @PostMapping(value = "/notify/onlinehearing", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> catchCohEvent(@RequestBody CohEvent request) {
        if (request == null
                || request.getCaseId() == null
                || request.getOnlineHearingId() == null
                || request.getEventType() == null) {
            //throw a bad request error
            String errorMessage = request == null
                    ? "null request object" :
                    String.format("case id [%s] online hearing id [%s] event type [%s]", request.getCaseId(), request.getOnlineHearingId(), request.getEventType());
            log.info("Bad request to handle COH event " + errorMessage);
            return ResponseEntity.badRequest().build();
        }
        String eventType = request.getEventType();

        String onlineHearingId = request.getOnlineHearingId();

        Long caseId;
        try {
            caseId = Long.valueOf(request.getCaseId());
        } catch (NumberFormatException e) {
            String errorMessage = "Case id received as invalid number: " + request.getCaseId();
            log.error(errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }

        log.info("Received event [{}] for case [{}] and hearing [{}]",
                eventType, caseId, onlineHearingId);

        if (cohEventActionMapper.handle(request)) {
            log.info("Handled event [{}] for case [{}] and hearing [{}]",eventType, caseId, onlineHearingId);
            return ResponseEntity.ok("");
        } else {
            String errorMessage = "Event [" + request.getEventType() + "] not mapped";
            log.info("Bad request to handle COH event " + errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }
    }

}
