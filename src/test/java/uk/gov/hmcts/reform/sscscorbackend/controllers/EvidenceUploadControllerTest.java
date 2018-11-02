package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;
import uk.gov.hmcts.reform.sscscorbackend.service.EvidenceUploadService;
import uk.gov.hmcts.reform.sscscorbackend.service.documentmanagement.IllegalFileTypeException;

public class EvidenceUploadControllerTest {

    private EvidenceUploadService evidenceUploadService;
    private EvidenceUploadController evidenceUploadController;
    private String someOnlineHearingId;
    private String someQuestionId;
    private String someEvidenceId;
    private Evidence evidence;

    @Before
    public void setUp() {
        evidenceUploadService = mock(EvidenceUploadService.class);
        evidenceUploadController = new EvidenceUploadController(evidenceUploadService);
        someOnlineHearingId = "someOnlineHearingId";
        someQuestionId = "someQuestionId";
        someEvidenceId = "someEvidenceId";
        evidence = mock(Evidence.class);
    }

    @Test
    public void canUploadEvidence() {
        MultipartFile file = mock(MultipartFile.class);
        when(evidenceUploadService.uploadEvidence(someOnlineHearingId, someQuestionId, file)).thenReturn(of(evidence));

        ResponseEntity<Evidence> evidenceResponseEntity = evidenceUploadController.uploadEvidence(
                someOnlineHearingId, someQuestionId, file
        );

        assertThat(evidenceResponseEntity.getStatusCode(), is(OK));
    }

    @Test
    public void cannotUploadEvidenceWhenOnlineHearingDoesNotExist() {
        MultipartFile file = mock(MultipartFile.class);
        when(evidenceUploadService.uploadEvidence(someOnlineHearingId, someQuestionId, file)).thenReturn(empty());

        ResponseEntity<Evidence> evidenceResponseEntity = evidenceUploadController.uploadEvidence(
                someOnlineHearingId, someQuestionId, file
        );

        assertThat(evidenceResponseEntity.getStatusCode(), is(NOT_FOUND));
    }

    @Test
    public void cannotUploadDocumentsThatDocumentStoreDoesNotSupport() {
        MultipartFile file = mock(MultipartFile.class);
        when(evidenceUploadService.uploadEvidence(someOnlineHearingId, someQuestionId, file)).thenThrow(new IllegalFileTypeException("someFile.bad"));

        ResponseEntity<Evidence> evidenceResponseEntity = evidenceUploadController.uploadEvidence(
                someOnlineHearingId, someQuestionId, file
        );

        assertThat(evidenceResponseEntity.getStatusCode(), is(UNPROCESSABLE_ENTITY));
    }

    @Test
    public void canDeleteEvidence() {
        when(evidenceUploadService.deleteEvidence(someOnlineHearingId, someEvidenceId)).thenReturn(true);

        ResponseEntity evidenceResponseEntity = evidenceUploadController
                .deleteEvidence(someOnlineHearingId, someQuestionId, someEvidenceId);

        assertThat(evidenceResponseEntity.getStatusCode(), is(NO_CONTENT));
    }

    @Test
    public void cannotDeleteEvidenceWhenOnlineHearingDoesNotExist() {
        when(evidenceUploadService.deleteEvidence(someOnlineHearingId, someEvidenceId)).thenReturn(false);

        ResponseEntity evidenceResponseEntity = evidenceUploadController
                .deleteEvidence(someOnlineHearingId, someQuestionId, someEvidenceId);

        assertThat(evidenceResponseEntity.getStatusCode(), is(NOT_FOUND));
    }
}
