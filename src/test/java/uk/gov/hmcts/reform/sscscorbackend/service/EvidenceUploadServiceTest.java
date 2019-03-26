package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.documentmanagement.DocumentManagementService;

public class EvidenceUploadServiceTest {

    private EvidenceUploadService evidenceUploadService;
    private CorCcdService ccdService;
    private OnlineHearingService onlineHearingService;
    private String someOnlineHearingId;
    private String someQuestionId;
    private long someCcdCaseId;
    private IdamTokens idamTokens;
    private String fileName;
    private String documentUrl;
    private MultipartFile file;
    private final Date evidenceCreatedOn = new Date();
    private String someEvidenceId;

    @Before
    public void setUp() {
        ccdService = mock(CorCcdService.class);
        onlineHearingService = mock(OnlineHearingService.class);
        someOnlineHearingId = "someOnlinehearingId";
        someQuestionId = "someQuestionId";
        someEvidenceId = "someEvidenceId";

        someCcdCaseId = 123L;

        IdamService idamService = mock(IdamService.class);
        idamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);

        DocumentManagementService documentManagementService = mock(DocumentManagementService.class);
        evidenceUploadService = new EvidenceUploadService(
                documentManagementService,
                ccdService,
                idamService,
                onlineHearingService
        );
        fileName = "someFileName.txt";
        documentUrl = "http://example.com/document/" + someEvidenceId;
        file = mock(MultipartFile.class);

        UploadResponse uploadResponse = createUploadResponse();
        when(documentManagementService.upload(singletonList(file))).thenReturn(uploadResponse);
    }

    @Test
    public void uploadsEvidenceAndAddsItToDraftSscsDocumentsInCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        final int originalNumberOfSscsDocuments = sscsCaseDetails.getData().getDraftSscsDocument().size();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadDraftHearingEvidence(someOnlineHearingId, file);

        assertThat(evidenceOptional.isPresent(), is(true));
        Evidence evidence = evidenceOptional.get();
        assertThat(evidence, is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
        verify(ccdService).updateCase(
                hasDraftSscsDocument(originalNumberOfSscsDocuments, documentUrl, fileName),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence uploaded"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void submitsEvidenceAddsDraftSscsDocumentsToSscsDocumentsInCcdWhenThereAreNoSscsDocuments() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        boolean submittedEvidence = evidenceUploadService.submitHearingEvidence(someOnlineHearingId);

        assertThat(submittedEvidence, is(true));
        verify(ccdService).updateCase(
                and(hasSscsDocument(0, documentUrl, fileName), doesNotHaveDraftSscsDocuments()),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence uploaded"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void submitsEvidenceAddsDraftSscsDocumentsToSscsDocumentsInCcdWhenThereAreOtherSscsDocuments() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        List<SscsDocument> list = singletonList(SscsDocument.builder()
                .value(SscsDocumentDetails.builder()
                        .documentFileName("anotherFileName")
                        .documentLink(DocumentLink.builder()
                                .documentUrl("http://anotherUrl")
                                .build())
                        .documentDateAdded(convertCreatedOnDate(evidenceCreatedOn))
                        .build())
                .build());
        sscsCaseDetails.getData().setSscsDocument(list);

        final int originalNumberOfSscsDocuments = sscsCaseDetails.getData().getSscsDocument().size();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        boolean submittedEvidence = evidenceUploadService.submitHearingEvidence(someOnlineHearingId);

        assertThat(submittedEvidence, is(true));
        verify(ccdService).updateCase(
                and(hasSscsDocument(originalNumberOfSscsDocuments, documentUrl, fileName), doesNotHaveDraftSscsDocuments()),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence uploaded"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void uploadsEvidenceWhenThereAreNotAlreadySscsDocumentsInCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithoutCcdDocuments();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadDraftHearingEvidence(someOnlineHearingId, file);

        assertThat(evidenceOptional.isPresent(), is(true));
        Evidence evidence = evidenceOptional.get();
        assertThat(evidence, is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
        verify(ccdService).updateCase(
                hasDraftSscsDocument(0, documentUrl, fileName),
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
        when(onlineHearingService.getCcdCase(nonExistentHearingId)).thenReturn(Optional.empty());

        Optional<Evidence> evidence = evidenceUploadService.uploadDraftHearingEvidence(nonExistentHearingId, file);

        assertThat(evidence.isPresent(), is(false));
    }

    @Test
    public void uploadsEvidenceToQuestionAndAddsItToCorDocumentsInCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        final int originalNumberOfCorDocuments = sscsCaseDetails.getData().getCorDocument().size();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadQuestionEvidence(someOnlineHearingId, someQuestionId, file);

        assertThat(evidenceOptional.isPresent(), is(true));
        Evidence evidence = evidenceOptional.get();
        assertThat(evidence, is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
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
    public void uploadsEvidenceToQuestionWhenThereAreNotAlreadyCorDocumentsInCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithoutCcdDocuments();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadQuestionEvidence(someOnlineHearingId, someQuestionId, file);

        assertThat(evidenceOptional.isPresent(), is(true));
        Evidence evidence = evidenceOptional.get();
        assertThat(evidence, is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
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
    public void uploadEvidenceToQuestionForAHearingThatDoesNotExist() {
        String nonExistentHearingId = "nonExistentHearingId";
        when(onlineHearingService.getCcdCase(nonExistentHearingId)).thenReturn(Optional.empty());

        Optional<Evidence> evidence = evidenceUploadService.uploadQuestionEvidence(nonExistentHearingId, someQuestionId, file);

        assertThat(evidence.isPresent(), is(false));
    }

    @Test
    public void listEvidence() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));


        List<Evidence> evidenceList = evidenceUploadService.listQuestionEvidence(someOnlineHearingId, someQuestionId);

        assertThat(evidenceList.size(), is(1));
        assertThat(evidenceList.get(0), is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
    }

    @Test
    public void listEvidenceWhenNoEvidenceHasBeenUploaded() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithoutCcdDocuments();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        List<Evidence> evidenceList = evidenceUploadService.listQuestionEvidence(someOnlineHearingId, someQuestionId);

        assertThat(evidenceList.isEmpty(), is(true));
    }

    @Test
    public void listEvidenceWhenEvidenceHasOnlyBeenUploadedForOtherQuestions() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails("someOtherQuestionId", fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        List<Evidence> evidenceList = evidenceUploadService.listQuestionEvidence(someOnlineHearingId, someQuestionId);

        assertThat(evidenceList.isEmpty(), is(true));
    }

    @Test
    public void listEvidenceForAHearingThatDoesNotExist() {
        String nonExistentHearingId = "nonExistentHearingId";
        when(onlineHearingService.getCcdCase(nonExistentHearingId)).thenReturn(Optional.empty());

        List<Evidence> evidenceList = evidenceUploadService.listQuestionEvidence(nonExistentHearingId, someQuestionId);

        assertThat(evidenceList, is(emptyList()));
    }

    @Test
    public void deleteEvidenceFromCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        boolean hearingFound = evidenceUploadService.deleteDraftHearingEvidence(someOnlineHearingId, someEvidenceId);

        assertThat(hearingFound, is(true));
        verify(ccdService).updateCase(
                doesNotHaveDraftSscsDocuments(),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence deleted"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void deleteEvidenceIfCaseHadNoEvidence() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithoutCcdDocuments();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        boolean hearingFound = evidenceUploadService.deleteDraftHearingEvidence(someOnlineHearingId, someEvidenceId);

        assertThat(hearingFound, is(true));
        verify(ccdService, never()).updateCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void deleteEvidenceForAHearingThatDoesNotExist() {
        String nonExistentHearingId = "nonExistentHearingId";
        when(onlineHearingService.getCcdCase(nonExistentHearingId)).thenReturn(Optional.empty());

        boolean hearingFound = evidenceUploadService.deleteDraftHearingEvidence(nonExistentHearingId, someEvidenceId);

        assertThat(hearingFound, is(false));
    }

    @Test
    public void deleteEvidenceForQuestionFromCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        boolean hearingFound = evidenceUploadService.deleteQuestionEvidence(someOnlineHearingId, someEvidenceId);

        assertThat(hearingFound, is(true));
        verify(ccdService).updateCase(
                doesNotHaveCorDocuments(),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence deleted"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void deleteEvidenceForQuestionIfCaseHadNoCohEvidence() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithoutCcdDocuments();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        boolean hearingFound = evidenceUploadService.deleteQuestionEvidence(someOnlineHearingId, someEvidenceId);

        assertThat(hearingFound, is(true));
        verify(ccdService, never()).updateCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void deleteEvidenceForQuestionForAHearingThatDoesNotExist() {
        String nonExistentHearingId = "nonExistentHearingId";
        when(onlineHearingService.getCcdCase(nonExistentHearingId)).thenReturn(Optional.empty());

        boolean hearingFound = evidenceUploadService.deleteQuestionEvidence(nonExistentHearingId, someEvidenceId);

        assertThat(hearingFound, is(false));
    }

    private UploadResponse createUploadResponse() {
        Document document = new Document();
        document.createdOn = evidenceCreatedOn;
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

    private SscsCaseData hasSscsDocument(int originalNumberOfDocuments, String documentUrl, String fileName) {
        return argThat(argument -> {
            List<SscsDocument> sscsDocument = argument.getSscsDocument();
            return sscsDocument.size() == originalNumberOfDocuments + 1 &&
                    sscsDocument.get(originalNumberOfDocuments).getValue().getDocumentLink().getDocumentUrl().equals(documentUrl) &&
                    sscsDocument.get(originalNumberOfDocuments).getValue().getDocumentFileName().equals(fileName);
        });
    }

    private SscsCaseData hasDraftSscsDocument(int originalNumberOfDocuments, String documentUrl, String fileName) {
        return argThat(argument -> {
            List<SscsDocument> sscsDocument = argument.getDraftSscsDocument();
            return sscsDocument.size() == originalNumberOfDocuments + 1 &&
                    sscsDocument.get(originalNumberOfDocuments).getValue().getDocumentLink().getDocumentUrl().equals(documentUrl) &&
                    sscsDocument.get(originalNumberOfDocuments).getValue().getDocumentFileName().equals(fileName);
        });
    }

    private SscsCaseData doesNotHaveCorDocuments() {
        return argThat(argument -> {
            List<CorDocument> corDocument = argument.getCorDocument();
            return corDocument.isEmpty();
        });
    }

    private SscsCaseData doesNotHaveDraftSscsDocuments() {
        return argThat(argument -> {
            List<SscsDocument> sscsDocument = argument.getDraftSscsDocument();
            return sscsDocument.isEmpty();
        });
    }

    private SscsCaseDetails createSscsCaseDetails(String someQuestionId, String fileName, String documentUrl, Date evidenceCreatedOn) {
        return SscsCaseDetails.builder()
                .id(someCcdCaseId)
                .data(SscsCaseData.builder()
                        .corDocument(singletonList(CorDocument.builder()
                                .value(CorDocumentDetails.builder()
                                        .questionId(someQuestionId)
                                        .document(SscsDocumentDetails.builder()
                                                .documentFileName(fileName)
                                                .documentLink(DocumentLink.builder()
                                                        .documentUrl(documentUrl)
                                                        .build())
                                                .documentDateAdded(convertCreatedOnDate(evidenceCreatedOn))
                                                .build())
                                        .build())
                                .build()))
                        .draftSscsDocument(singletonList(SscsDocument.builder()
                                .value(SscsDocumentDetails.builder()
                                        .documentFileName(fileName)
                                        .documentLink(DocumentLink.builder()
                                                .documentUrl(documentUrl)
                                                .build())
                                        .documentDateAdded(convertCreatedOnDate(evidenceCreatedOn))
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    private SscsCaseDetails createSscsCaseDetailsWithoutCcdDocuments() {
        return SscsCaseDetails.builder().id(someCcdCaseId).data(SscsCaseData.builder().build()).build();
    }

    private String convertCreatedOnDate(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ISO_DATE);
    }
}
