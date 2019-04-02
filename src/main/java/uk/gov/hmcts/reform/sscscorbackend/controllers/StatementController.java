package uk.gov.hmcts.reform.sscscorbackend.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.Statement;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreAppellantStatementService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.AppellantStatementPdfData;

@Slf4j
@RestController
@RequestMapping("/continuous-online-hearings")
public class StatementController {
    private final StoreAppellantStatementService storeAppellantStatementService;
    private final OnlineHearingService onlineHearingService;

    @Autowired
    public StatementController(StoreAppellantStatementService storeAppellantStatementService, OnlineHearingService onlineHearingService) {
        this.storeAppellantStatementService = storeAppellantStatementService;
        this.onlineHearingService = onlineHearingService;
    }

    @ApiOperation(value = "Upload COR personal statement",
            notes = "Uploads a personal statement for a COR appeal. You need to have an appeal in CCD and an online hearing in COH " +
                    "that references the appeal in CCD. The statement is saved as a piece of evidence for the case in CCD as a PDF."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Statement has been added to the appeal"),
            @ApiResponse(code = 404, message = "No online hearing found with online hearing id")
    })
    @PostMapping(
            value = "/{onlineHearingId}/statement",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity uploadStatement(
            @PathVariable("onlineHearingId") String onlineHearingId,
            @RequestBody Statement statement) {

        return onlineHearingService.getCcdCase(onlineHearingId).map(caseDetails -> {
            storeAppellantStatementService.storePdf(caseDetails.getId(), onlineHearingId, new AppellantStatementPdfData(caseDetails, statement));
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
