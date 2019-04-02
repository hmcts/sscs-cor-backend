package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.unprocessableEntity;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;
import uk.gov.hmcts.reform.sscscorbackend.service.EvidenceUploadService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.documentmanagement.IllegalFileTypeException;

@Slf4j
@RestController
@RequestMapping("/continuous-online-hearings")
public class EvidenceUploadController {
    private final EvidenceUploadService evidenceUploadService;

    @Autowired
    public EvidenceUploadController(EvidenceUploadService evidenceUploadService) {
        this.evidenceUploadService = evidenceUploadService;
    }

    @ApiOperation(value = "Upload COR evidence",
            notes = "Uploads evidence for a COR appeal which will be held in a draft state against the case that is not " +
                    "visible by a caseworker in CCD. You will need to submit the evidence for it to be visbale in CCD " +
                    "by a caseworker. You need to have an appeal in CCD and an online hearing in COH that references " +
                    "the appeal in CCD."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evidence has been added to the appeal"),
            @ApiResponse(code = 404, message = "No online hearing found with online hearing id"),
            @ApiResponse(code = 422, message = "The file cannot be added to the document store")
    })
    @RequestMapping(
            value = "{onlineHearingId}/evidence",
            method = RequestMethod.PUT,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity<Evidence> uploadEvidence(
            @PathVariable("onlineHearingId") String onlineHearingId,
            @RequestParam("file") MultipartFile file
    ) {
        return uploadEvidence(() -> evidenceUploadService.uploadDraftHearingEvidence(onlineHearingId, file));
    }

    @ApiOperation(value = "Upload COR evidence to a question",
            notes = "Uploads evidence for a COR appeal. You need to have an appeal in CCD and an online hearing in COH " +
                    "that references the appeal in CCD."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Evidence has been added to the appeal"),
            @ApiResponse(code = 404, message = "No online hearing found with online hearing id"),
            @ApiResponse(code = 422, message = "The file cannot be added to the document store")
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
        return uploadEvidence(() -> evidenceUploadService.uploadQuestionEvidence(onlineHearingId, questionId, file));
    }

    private ResponseEntity<Evidence> uploadEvidence(Supplier<Optional<Evidence>> uploadEvidence) {
        try {
            Optional<Evidence> evidenceOptional = uploadEvidence.get();
            return evidenceOptional.map(ResponseEntity::ok)
                    .orElse(notFound().build());
        } catch (IllegalFileTypeException exc) {
            log.info("Cannot upload file illegal file type", exc);
            return unprocessableEntity().build();
        }
    }

    @ApiOperation(value = "List evidence for a hearing",
            notes = "Lists the evidence that has already been uploaded for a hearing but is still in a draft state."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of draft evidence")
    })
    @RequestMapping(
            value = "{onlineHearingId}/evidence",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity<List<Evidence>> listDraftEvidence(
            @PathVariable("onlineHearingId") String onlineHearingId
    ) {
        return ResponseEntity.ok(evidenceUploadService.listDraftHearingEvidence(onlineHearingId));
    }

    @ApiOperation(value = "Delete COR evidence",
            notes = "Deletes evidence for a COR appeal. You need to have an appeal in CCD and an online hearing in COH " +
                    "that references the appeal in CCD."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Evidence deleted "),
            @ApiResponse(code = 404, message = "No online hearing found with online hearing id"),
    })
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(
            value = "{onlineHearingId}/evidence/{evidenceId}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity deleteEvidence(
            @PathVariable("onlineHearingId") String onlineHearingId,
            @PathVariable("evidenceId") String evidenceId
    ) {
        boolean hearingFound = evidenceUploadService.deleteDraftHearingEvidence(onlineHearingId, evidenceId);
        return hearingFound ? ResponseEntity.noContent().build() : notFound().build();
    }

    @ApiOperation(value = "Delete COR evidence from a question",
            notes = "Deletes evidence for a COR appeal. You need to have an appeal in CCD and an online hearing in COH " +
                    "that references the appeal in CCD."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Evidence deleted "),
            @ApiResponse(code = 404, message = "No online hearing found with online hearing id"),
    })
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(
            value = "{onlineHearingId}/questions/{questionId}/evidence/{evidenceId}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity deleteEvidence(
            @PathVariable("onlineHearingId") String onlineHearingId,
            @PathVariable("questionId") String questionId,
            @PathVariable("evidenceId") String evidenceId
    ) {
        boolean hearingFound = evidenceUploadService.deleteQuestionEvidence(onlineHearingId, evidenceId);
        return hearingFound ? ResponseEntity.noContent().build() : notFound().build();
    }

    @ApiOperation(value = "Submit COR evidence",
            notes = "Submits the evidence that has already been uploaded in a draft state. This means it will be " +
                    "visible in CCD by a caseworker and JUI by the pannel members. You need to have an appeal in CCD " +
                    "and an online hearing in COH that references the appeal in CCD."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Evidence has been submitted to the appeal"),
            @ApiResponse(code = 404, message = "No online hearing found with online hearing id")
    })
    @RequestMapping(
            value = "{onlineHearingId}/evidence",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity submitEvidence(
            @PathVariable("onlineHearingId") String onlineHearingId
    ) {
        boolean evidenceSubmitted = evidenceUploadService.submitHearingEvidence(onlineHearingId);
        return evidenceSubmitted ? ResponseEntity.noContent().build() : notFound().build();
    }
}
