package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;
import uk.gov.hmcts.reform.sscscorbackend.service.EvidenceUploadService;

public class EvidenceUploadControllerTest {

    private EvidenceUploadService evidenceUploadService;
    private EvidenceUploadController evidenceUploadController;
    private String someOnlineHearingId;
    private String someQuestionId;
    private Evidence evidence;

    @Before
    public void setUp() {
        evidenceUploadService = mock(EvidenceUploadService.class);
        evidenceUploadController = new EvidenceUploadController(evidenceUploadService);
        someOnlineHearingId = "someOnlineHearingId";
        someQuestionId = "someQuestionId";
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
}
