package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.http.ResponseEntity.notFound;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;
import uk.gov.hmcts.reform.sscscorbackend.service.EvidenceUploadService;

@RestController
@RequestMapping("/continuous-online-hearings")
public class EvidenceUploadController {
    private final EvidenceUploadService evidenceUploadService;

    @Autowired
    public EvidenceUploadController(EvidenceUploadService evidenceUploadService) {
        this.evidenceUploadService = evidenceUploadService;
    }

    @ApiOperation(value = "Upload COR evidence",
            notes = "Uploads evidence for a COR appeal. You need to have an appeal in CCD and an online hearing in COH " +
                    "that references the appeal in CCD. "
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No online hearing found with online hearing id "),
    })
    @RequestMapping(
            value = "{onlineHearingId}/questions/{questionId}/evidence",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity<Evidence> uploadEvidence(
            @PathVariable("onlineHearingId") String onlineHearingId,
            @PathVariable("questionId") String questionId,
            @RequestParam("file") MultipartFile file
    ) {
        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadEvidence(onlineHearingId, questionId, file);
        return evidenceOptional.map(ResponseEntity::ok)
                .orElse(notFound().build());
    }
}
