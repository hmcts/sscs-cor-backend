package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;



@RestController
public class OnlineHearingController {
    private OnlineHearingService onlineHearingService;

    public OnlineHearingController(@Autowired OnlineHearingService onlineHearingService) {
        this.onlineHearingService = onlineHearingService;
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
    @RequestMapping(value = "/notify/onlinehearing", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
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

        onlineHearingService.storeOnlineHearingInCcd(onlineHearingId, caseId);

        return ResponseEntity.ok("");
    }
}
