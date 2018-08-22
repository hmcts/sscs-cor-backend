package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.Panel;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;



@RestController
public class OnlineHearingController {
    private OnlineHearingService onlineHearingService;

    public OnlineHearingController(@Autowired OnlineHearingService onlineHearingService) {
        this.onlineHearingService = onlineHearingService;
    }

    @RequestMapping(value = "/notify/onlineappeal", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> catchEvent(@RequestBody CcdEvent request) {
        if (request == null
            || request.getCaseDetails() == null
            || request.getCaseDetails().getCaseId() == null
            || request.getCaseDetails().getCaseData() == null
            || request.getCaseDetails().getCaseData().getOnlinePanel() == null) {
            //throw a bad request error
            return ResponseEntity.badRequest().build();
        }

        String caseId = request.getCaseDetails().getCaseId();

        Panel panel = request.getCaseDetails().getCaseData().getOnlinePanel();

        String onlineHearingId = onlineHearingService.createOnlineHearing(caseId, panel);

        return ResponseEntity.ok(onlineHearingId);
    }
}
