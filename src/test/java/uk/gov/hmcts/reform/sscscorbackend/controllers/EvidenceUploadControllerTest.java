package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;
import uk.gov.hmcts.reform.sscscorbackend.service.EvidenceUploadService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.documentmanagement.IllegalFileTypeException;

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
        when(evidenceUploadService.uploadDraftHearingEvidence(someOnlineHearingId, file)).thenReturn(of(evidence));

        ResponseEntity<Evidence> evidenceResponseEntity = evidenceUploadController.uploadEvidence(someOnlineHearingId, file);

        assertThat(evidenceResponseEntity.getStatusCode(), is(OK));
    }

    @Test
    public void cannotUploadEvidenceWhenOnlineHearingDoesNotExist() {
        MultipartFile file = mock(MultipartFile.class);
        when(evidenceUploadService.uploadDraftHearingEvidence(someOnlineHearingId, file)).thenReturn(empty());

        ResponseEntity<Evidence> evidenceResponseEntity = evidenceUploadController.uploadEvidence(someOnlineHearingId, file);

        assertThat(evidenceResponseEntity.getStatusCode(), is(NOT_FOUND));
    }

    @Test
    public void cannotUploadDocumentsThatDocumentStoreDoesNotSupport() {
        MultipartFile file = mock(MultipartFile.class);
        when(evidenceUploadService.uploadDraftHearingEvidence(someOnlineHearingId, file)).thenThrow(new IllegalFileTypeException("someFile.bad"));

        ResponseEntity<Evidence> evidenceResponseEntity = evidenceUploadController.uploadEvidence(someOnlineHearingId, file);

        assertThat(evidenceResponseEntity.getStatusCode(), is(UNPROCESSABLE_ENTITY));
    }

    @Test
    public void canUploadEvidenceToQuestion() {
        MultipartFile file = mock(MultipartFile.class);
        when(evidenceUploadService.uploadQuestionEvidence(someOnlineHearingId, someQuestionId, file)).thenReturn(of(evidence));

        ResponseEntity<Evidence> evidenceResponseEntity = evidenceUploadController.uploadEvidence(
                someOnlineHearingId, someQuestionId, file
        );

        assertThat(evidenceResponseEntity.getStatusCode(), is(OK));
    }

    @Test
    public void cannotUploadEvidenceToQuestionWhenOnlineHearingDoesNotExist() {
        MultipartFile file = mock(MultipartFile.class);
        when(evidenceUploadService.uploadQuestionEvidence(someOnlineHearingId, someQuestionId, file)).thenReturn(empty());

        ResponseEntity<Evidence> evidenceResponseEntity = evidenceUploadController.uploadEvidence(
                someOnlineHearingId, someQuestionId, file
        );

        assertThat(evidenceResponseEntity.getStatusCode(), is(NOT_FOUND));
    }

    @Test
    public void cannotUploadDocumentsToQuestionThatDocumentStoreDoesNotSupport() {
        MultipartFile file = mock(MultipartFile.class);
        when(evidenceUploadService.uploadQuestionEvidence(someOnlineHearingId, someQuestionId, file)).thenThrow(new IllegalFileTypeException("someFile.bad"));

        ResponseEntity<Evidence> evidenceResponseEntity = evidenceUploadController.uploadEvidence(
                someOnlineHearingId, someQuestionId, file
        );

        assertThat(evidenceResponseEntity.getStatusCode(), is(UNPROCESSABLE_ENTITY));
    }

    @Test
    public void canDeleteEvidence() {
        when(evidenceUploadService.deleteDraftHearingEvidence(someOnlineHearingId, someEvidenceId)).thenReturn(true);

        ResponseEntity evidenceResponseEntity = evidenceUploadController
                .deleteEvidence(someOnlineHearingId, someEvidenceId);

        assertThat(evidenceResponseEntity.getStatusCode(), is(NO_CONTENT));
    }

    @Test
    public void cannotDeleteEvidenceWhenOnlineHearingDoesNotExist() {
        when(evidenceUploadService.deleteDraftHearingEvidence(someOnlineHearingId, someEvidenceId)).thenReturn(false);

        ResponseEntity evidenceResponseEntity = evidenceUploadController
                .deleteEvidence(someOnlineHearingId, someEvidenceId);

        assertThat(evidenceResponseEntity.getStatusCode(), is(NOT_FOUND));
    }

    @Test
    public void canDeleteEvidenceForQuestion() {
        when(evidenceUploadService.deleteQuestionEvidence(someOnlineHearingId, someEvidenceId)).thenReturn(true);

        ResponseEntity evidenceResponseEntity = evidenceUploadController
                .deleteEvidence(someOnlineHearingId, someQuestionId, someEvidenceId);

        assertThat(evidenceResponseEntity.getStatusCode(), is(NO_CONTENT));
    }

    @Test
    public void cannotDeleteEvidenceForQuestionWhenOnlineHearingDoesNotExist() {
        when(evidenceUploadService.deleteQuestionEvidence(someOnlineHearingId, someEvidenceId)).thenReturn(false);

        ResponseEntity evidenceResponseEntity = evidenceUploadController
                .deleteEvidence(someOnlineHearingId, someQuestionId, someEvidenceId);

        assertThat(evidenceResponseEntity.getStatusCode(), is(NOT_FOUND));
    }

    @Test
    public void submitEvidence() {
        when(evidenceUploadService.submitHearingEvidence(someOnlineHearingId)).thenReturn(true);

        ResponseEntity responseEntity = evidenceUploadController.submitEvidence(someOnlineHearingId);

        assertThat(responseEntity.getStatusCode(), is(NO_CONTENT));
    }

    @Test
    public void submitEvidenceWhenHearingDoesNotExist() {
        when(evidenceUploadService.submitHearingEvidence(someOnlineHearingId)).thenReturn(false);

        ResponseEntity responseEntity = evidenceUploadController.submitEvidence(someOnlineHearingId);

        assertThat(responseEntity.getStatusCode(), is(NOT_FOUND));
    }

    @Test
    public void listEvidence() {
        List<Evidence> expectedEvidence = singletonList(evidence);
        when(evidenceUploadService.listDraftHearingEvidence(someOnlineHearingId)).thenReturn(expectedEvidence);

        ResponseEntity<List<Evidence>> listResponseEntity = evidenceUploadController.listDraftEvidence(someOnlineHearingId);

        assertThat(listResponseEntity.getStatusCode(), is(OK));
        assertThat(listResponseEntity.getBody(), is(expectedEvidence));
    }
}
