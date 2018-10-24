package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;

public class EvidenceUploadServiceTest {

    private EvidenceUploadService evidenceUploadService;
    private EvidenceManagementService evidenceManagementService;
    private CcdService ccdService;
    private IdamService idamService;
    private OnlineHearingService onlineHearingService;
    private String someOnlineHearingId;
    private String someQuestionId;
    private long someCcdCaseId;
    private IdamTokens idamTokens;
    private String fileName;
    private String documentUrl;
    private MultipartFile file;

    @Before
    public void setUp() {
        evidenceManagementService = mock(EvidenceManagementService.class);
        ccdService = mock(CcdService.class);
        idamService = mock(IdamService.class);
        onlineHearingService = mock(OnlineHearingService.class);
        someOnlineHearingId = "someOnlinehearingId";
        someQuestionId = "someQuestionId";

        someCcdCaseId = 123L;
        when(onlineHearingService.getCcdCaseId(someOnlineHearingId)).thenReturn(Optional.of(someCcdCaseId));

        idamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);

        evidenceUploadService = new EvidenceUploadService(
                evidenceManagementService,
                ccdService,
                idamService,
                onlineHearingService
        );
        fileName = "someFileName.txt";
        documentUrl = "http://example.com/someDocumentUrl";
        file = mock(MultipartFile.class);

        UploadResponse uploadResponse = createUploadResponse();
        when(evidenceManagementService.upload(singletonList(file))).thenReturn(uploadResponse);
    }

    @Test
    public void uploadsEvidenceAndAddsItToCorDocumentsInCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl);
        final int originalNumberOfCorDocuments = sscsCaseDetails.getData().getCorDocument().size();
        when(ccdService.getByCaseId(someCcdCaseId, idamTokens)).thenReturn(sscsCaseDetails);

        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadEvidence(someOnlineHearingId, someQuestionId, file);

        assertThat(evidenceOptional.isPresent(), is(true));
        Evidence evidence = evidenceOptional.get();
        assertThat(evidence, is(new Evidence(documentUrl, fileName)));
        verify(ccdService).updateCase(
                hasCorDocument(originalNumberOfCorDocuments, someQuestionId, documentUrl, fileName),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence uploaded"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void uploadsEvidenceWhenThereAreNotAlreadyCorDocumentsInCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithoutCcdDocuments();
        when(ccdService.getByCaseId(someCcdCaseId, idamTokens)).thenReturn(sscsCaseDetails);

        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadEvidence(someOnlineHearingId, someQuestionId, file);

        assertThat(evidenceOptional.isPresent(), is(true));
        Evidence evidence = evidenceOptional.get();
        assertThat(evidence, is(new Evidence(documentUrl, fileName)));
        verify(ccdService).updateCase(
                hasCorDocument(0, someQuestionId, documentUrl, fileName),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence uploaded"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void uploadEvidenceForAHearingThatDoesNotExist() {
        String nonExistentHearingId = "nonExistentHearingId";
        when(onlineHearingService.getCcdCaseId(nonExistentHearingId)).thenReturn(Optional.empty());

        Optional<Evidence> evidence = evidenceUploadService.uploadEvidence(nonExistentHearingId, someQuestionId, file);

        assertThat(evidence.isPresent(), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void uploadEvidenceForACaseThatDoesNotExistInCcd() {
        when(ccdService.getByCaseId(someCcdCaseId, idamTokens)).thenReturn(null);

        evidenceUploadService.uploadEvidence(someOnlineHearingId, someQuestionId, file);
    }

    @Test
    public void listEvidence() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl);
        when(ccdService.getByCaseId(someCcdCaseId, idamTokens)).thenReturn(sscsCaseDetails);


        Optional<List<Evidence>> evidence = evidenceUploadService.listEvidence(someOnlineHearingId, someQuestionId);

        assertThat(evidence.isPresent(), is(true));
        List<Evidence> evidenceList = evidence.get();
        assertThat(evidenceList.size(), is(1));
        assertThat(evidenceList.get(0), is(new Evidence(documentUrl, fileName)));
    }

    @Test
    public void listEvidenceWhenNoEvidenceHasBeenUploaded() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithoutCcdDocuments();
        when(ccdService.getByCaseId(someCcdCaseId, idamTokens)).thenReturn(sscsCaseDetails);

        Optional<List<Evidence>> evidence = evidenceUploadService.listEvidence(someOnlineHearingId, someQuestionId);

        assertThat(evidence.isPresent(), is(true));
        List<Evidence> evidenceList = evidence.get();
        assertThat(evidenceList.isEmpty(), is(true));
    }

    @Test
    public void listEvidenceWhenEvidenceHasOnlyBeenUploadedForOtherQuestions() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails("someOtherQuestionId", fileName, documentUrl);
        when(ccdService.getByCaseId(someCcdCaseId, idamTokens)).thenReturn(sscsCaseDetails);

        Optional<List<Evidence>> evidence = evidenceUploadService.listEvidence(someOnlineHearingId, someQuestionId);

        assertThat(evidence.isPresent(), is(true));
        List<Evidence> evidenceList = evidence.get();
        assertThat(evidenceList.isEmpty(), is(true));
    }

    @Test
    public void listEvidenceForAHearingThatDoesNotExist() {
        String nonExistentHearingId = "nonExistentHearingId";
        when(onlineHearingService.getCcdCaseId(nonExistentHearingId)).thenReturn(Optional.empty());

        Optional<List<Evidence>> evidence = evidenceUploadService.listEvidence(nonExistentHearingId, someQuestionId);

        assertThat(evidence.isPresent(), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void listEvidenceForACaseThatDoesNotExistInCcd() {
        when(ccdService.getByCaseId(someCcdCaseId, idamTokens)).thenReturn(null);

        evidenceUploadService.listEvidence(someOnlineHearingId, someQuestionId);
    }

    private UploadResponse createUploadResponse() {
        Document document = new Document();
        document.createdOn = new Date();
        document.links = new Document.Links();
        document.links.self = new Document.Link();
        document.links.self.href = documentUrl;
        document.originalDocumentName = fileName;
        UploadResponse.Embedded embedded = mock(UploadResponse.Embedded.class);
        when(embedded.getDocuments()).thenReturn(Collections.singletonList(document));
        UploadResponse uploadResponse = mock(UploadResponse.class);
        when(uploadResponse.getEmbedded()).thenReturn(embedded);
        return uploadResponse;
    }

    private SscsCaseData hasCorDocument(int originalNumberOfCorDocuments, String someQuestionId, String documentUrl, String fileName) {
        return argThat(argument -> {
            List<CorDocument> corDocument = argument.getCorDocument();
            return corDocument.size() == originalNumberOfCorDocuments + 1 &&
                    corDocument.get(originalNumberOfCorDocuments).getValue().getQuestionId().equals(someQuestionId) &&
                    corDocument.get(originalNumberOfCorDocuments).getValue().getDocument().getDocumentLink().getDocumentUrl().equals(documentUrl) &&
                    corDocument.get(originalNumberOfCorDocuments).getValue().getDocument().getDocumentFileName().equals(fileName);
        });
    }

    private SscsCaseDetails createSscsCaseDetails(String someQuestionId, String fileName, String documentUrl) {
        return SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
                        .corDocument(singletonList(CorDocument.builder()
                                .value(CorDocumentDetails.builder()
                                        .questionId(someQuestionId)
                                        .document(SscsDocumentDetails.builder()
                                                .documentFileName(fileName)
                                                .documentLink(DocumentLink.builder()
                                                        .documentUrl(documentUrl)
                                                        .build())
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    private SscsCaseDetails createSscsCaseDetailsWithoutCcdDocuments() {
        return SscsCaseDetails.builder().data(SscsCaseData.builder().build()).build();
    }
}