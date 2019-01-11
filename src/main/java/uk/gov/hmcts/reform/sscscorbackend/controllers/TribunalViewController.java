package uk.gov.hmcts.reform.sscscorbackend.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.TribunalViewResponse;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;

@RestController
@RequestMapping("/continuous-online-hearings")
public class TribunalViewController {
    private final OnlineHearingService onlineHearingService;

    public TribunalViewController(@Autowired OnlineHearingService onlineHearingService) {
        this.onlineHearingService = onlineHearingService;
    }

    @ApiOperation(value = "Appellant response to tribunal's view",
            notes = "Accepted or rejected with reasons"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Response recorded"),
            @ApiResponse(code = 400, message = "Missing reason for decision_rejected reply")
    })
    @RequestMapping(method = RequestMethod.PATCH, value = "{onlineHearingId}/tribunal-view")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> recordTribunalViewResponse(@PathVariable String onlineHearingId,
                                                           @RequestBody TribunalViewResponse tribunalViewResponse) {
        if (tribunalViewResponse.getReply().equals("decision_rejected") && tribunalViewResponse.getReason().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        // COH won't accept an empty reason, therefore set the reason to the reply if it's empty
        if (tribunalViewResponse.getReason().isEmpty()) {
            tribunalViewResponse.setReason(tribunalViewResponse.getReply());
        }
        onlineHearingService.addDecisionReply(onlineHearingId, tribunalViewResponse);
        return ResponseEntity.noContent().build();
    }
}
