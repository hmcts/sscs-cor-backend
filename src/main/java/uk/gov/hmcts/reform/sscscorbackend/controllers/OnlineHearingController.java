package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingTribunalsViewService;


@Slf4j
@RestController
public class OnlineHearingController {

    private final OnlineHearingService onlineHearingService;
    private final StoreOnlineHearingService storeOnlineHearingService;
    private final StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService;

    public OnlineHearingController(@Autowired OnlineHearingService onlineHearingService,
                                   @Autowired StoreOnlineHearingService storeOnlineHearingService,
                                   @Autowired StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService) {
        this.onlineHearingService = onlineHearingService;
        this.storeOnlineHearingService = storeOnlineHearingService;
        this.storeOnlineHearingTribunalsViewService = storeOnlineHearingTribunalsViewService;
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

        String caseId = request.getCaseDetails().getCaseId();

        String onlineHearingId = onlineHearingService.createOnlineHearing(caseId);

        return ResponseEntity.ok(onlineHearingId);
    }

    @ApiOperation(value = "Store online hearing details in CDM",
            notes = "Gets the details of the online hearing, e.g. the questions and answers, " +
                    "writes them into a pdf and attaches the pdf to the case in Core Data " +
                    "Management. This will get triggered by a COH event"
    )
    @PostMapping(value = "/notify/onlinehearing", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> catchCohEvent(@RequestBody CohEvent request) {
        if (request == null
                || request.getCaseId() == null
                || request.getOnlineHearingId() == null
                || request.getNotificationEventType() == null) {
            //throw a bad request error
            return ResponseEntity.badRequest().build();
        }

        String onlineHearingId = request.getOnlineHearingId();
        String caseId = request.getCaseId();
        log.info("Received event for storing online hearing for case {} and hearing {} ...",
                caseId, onlineHearingId);

        if (request.getNotificationEventType().equalsIgnoreCase("decision_issued")) {
            storeOnlineHearingTribunalsViewService.storeTribunalsView(Long.valueOf(caseId));
        } else {
            storeOnlineHearingService.storeOnlineHearingInCcd(onlineHearingId, caseId);
        }

        return ResponseEntity.ok("");
    }
}
