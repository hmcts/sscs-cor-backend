package uk.gov.hmcts.reform.sscscorbackend.service.evidence;

import static java.util.Arrays.asList;
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
import uk.gov.hmcts.reform.sscscorbackend.domain.EvidenceDescription;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreEvidenceDescriptionService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.EvidenceDescriptionPdfData;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence;
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
    private StoreEvidenceDescriptionService storeEvidenceDescriptionService;
    private EvidenceDescription someDescription;
    private EvidenceUploadEmailService evidenceUploadEmailService;

    @Before
    public void setUp() {
        ccdService = mock(CorCcdService.class);
        onlineHearingService = mock(OnlineHearingService.class);
        someOnlineHearingId = "someOnlinehearingId";
        someQuestionId = "someQuestionId";
        someEvidenceId = "someEvidenceId";

        someCcdCaseId = 123L;

        someDescription = new EvidenceDescription("some description");

        IdamService idamService = mock(IdamService.class);
        idamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);

        DocumentManagementService documentManagementService = mock(DocumentManagementService.class);
        storeEvidenceDescriptionService = mock(StoreEvidenceDescriptionService.class);
        evidenceUploadEmailService = mock(EvidenceUploadEmailService.class);
        evidenceUploadService = new EvidenceUploadService(
                documentManagementService,
                ccdService,
                idamService,
                onlineHearingService,
                storeEvidenceDescriptionService, evidenceUploadEmailService);
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
        UploadedEvidence evidenceDescriptionPdf = mock(UploadedEvidence.class);
        when(storeEvidenceDescriptionService.storePdf(
                someCcdCaseId,
                someOnlineHearingId,
                new EvidenceDescriptionPdfData(sscsCaseDetails, someDescription, singletonList(fileName))
        )).thenReturn(new CohEventActionContext(evidenceDescriptionPdf, sscsCaseDetails));
        List<SscsDocument> draftSscsDocument = sscsCaseDetails.getData().getDraftSscsDocument();

        boolean submittedEvidence = evidenceUploadService.submitHearingEvidence(someOnlineHearingId, someDescription);

        assertThat(submittedEvidence, is(true));
        verify(evidenceUploadEmailService).sendToDwp(evidenceDescriptionPdf, draftSscsDocument, sscsCaseDetails);
        verify(ccdService).updateCase(
                and(hasSscsDocument(0, documentUrl, fileName), doesNotHaveDraftSscsDocuments()),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence uploaded"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
        verify(storeEvidenceDescriptionService).storePdf(
                someCcdCaseId,
                someOnlineHearingId,
                new EvidenceDescriptionPdfData(sscsCaseDetails, someDescription, singletonList(fileName))
        );
    }

    @Test
    public void submitsEvidenceAddsDraftSscsDocumentsToSscsDocumentsInCcdWhenThereAreOtherSscsDocuments() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        SscsDocument descriptionDocument = SscsDocument.builder()
                .value(SscsDocumentDetails.builder()
                        .documentFileName("anotherFileName")
                        .documentLink(DocumentLink.builder()
                                .documentUrl("http://anotherUrl")
                                .build())
                        .documentDateAdded(convertCreatedOnDate(evidenceCreatedOn))
                        .build())
                .build();
        List<SscsDocument> list = singletonList(descriptionDocument);
        sscsCaseDetails.getData().setSscsDocument(list);

        final int originalNumberOfSscsDocuments = sscsCaseDetails.getData().getSscsDocument().size();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));
        UploadedEvidence evidenceDescriptionPdf = mock(UploadedEvidence.class);
        when(storeEvidenceDescriptionService.storePdf(
                someCcdCaseId,
                someOnlineHearingId,
                new EvidenceDescriptionPdfData(sscsCaseDetails, someDescription, singletonList(fileName))
        )).thenReturn(new CohEventActionContext(evidenceDescriptionPdf, sscsCaseDetails));
        List<SscsDocument> draftSscsDocument = sscsCaseDetails.getData().getDraftSscsDocument();

        boolean submittedEvidence = evidenceUploadService.submitHearingEvidence(someOnlineHearingId, someDescription);

        assertThat(submittedEvidence, is(true));
        verify(evidenceUploadEmailService).sendToDwp(evidenceDescriptionPdf, draftSscsDocument, sscsCaseDetails);
        verify(ccdService).updateCase(
                and(hasSscsDocument(originalNumberOfSscsDocuments, documentUrl, fileName), doesNotHaveDraftSscsDocuments()),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence uploaded"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );

        verify(storeEvidenceDescriptionService).storePdf(
                someCcdCaseId,
                someOnlineHearingId,
                new EvidenceDescriptionPdfData(sscsCaseDetails, someDescription, singletonList(fileName))
        );
    }

    @Test
    public void submitsQuestionEvidenceAddsDraftCorDocumentsToCorDocumentsInCcdWhenThereAreNoCorDocuments() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));
        when(storeEvidenceDescriptionService.storePdf(
                someCcdCaseId,
                someOnlineHearingId,
                new EvidenceDescriptionPdfData(sscsCaseDetails, someDescription, singletonList(fileName))
        )).thenReturn(new CohEventActionContext(mock(UploadedEvidence.class), sscsCaseDetails));

        boolean submittedEvidence = evidenceUploadService.submitQuestionEvidence(someOnlineHearingId, someQuestionId);

        assertThat(submittedEvidence, is(true));
        verify(ccdService).updateCase(
                and(hasCorDocument(0, someQuestionId, documentUrl, fileName), doesNotHaveDraftCorDocuments()),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence uploaded"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void submitsQuestionEvidenceAddsDraftCorDocumentsToCorDocumentsInCcdWhenThereAreOtherCorDocuments() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        List<CorDocument> list = singletonList(createCorDocument(fileName, documentUrl, someQuestionId));
        sscsCaseDetails.getData().setCorDocument(list);

        final int originalNumberOfSscsDocuments = sscsCaseDetails.getData().getCorDocument().size();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        boolean submittedEvidence = evidenceUploadService.submitQuestionEvidence(someOnlineHearingId, someQuestionId);

        assertThat(submittedEvidence, is(true));
        verify(ccdService).updateCase(
                and(hasCorDocument(originalNumberOfSscsDocuments, someQuestionId, documentUrl, fileName), doesNotHaveDraftCorDocuments()),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence uploaded"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void submitsQuestionEvidenceOnlyCopiesEvidenceForCorrectQuestion() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        String draftDocumentUrl = "http://url1";
        String draftFileName = "file1";
        String expectedFileName = "file2";
        String expectedUrl = "http://url2";
        String draftQuestionId = "1";
        List<CorDocument> list = asList(
                createCorDocument(draftFileName, draftDocumentUrl, draftQuestionId),
                createCorDocument(expectedFileName, expectedUrl, someQuestionId)
        );
        sscsCaseDetails.getData().setDraftCorDocument(list);

        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        boolean submittedEvidence = evidenceUploadService.submitQuestionEvidence(someOnlineHearingId, someQuestionId);

        assertThat(submittedEvidence, is(true));
        verify(ccdService).updateCase(
                and(hasCorDocument(0, someQuestionId, expectedUrl, expectedFileName), hasDraftCorDocument(0, draftQuestionId, draftDocumentUrl, draftFileName)),
                eq(someCcdCaseId),
                eq("uploadCorDocument"),
                eq("SSCS - cor evidence uploaded"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void submitsQuestionEvidenceHandlesNoEvidence() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        sscsCaseDetails.getData().setDraftCorDocument(null);
        sscsCaseDetails.getData().setCorDocument(null);

        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        boolean submittedEvidence = evidenceUploadService.submitQuestionEvidence(someOnlineHearingId, someQuestionId);

        assertThat(submittedEvidence, is(true));
        verifyZeroInteractions(ccdService);
    }

    private CorDocument createCorDocument(String fileName, String documentUrl, String questionId) {
        return CorDocument.builder()
                .value(CorDocumentDetails.builder()
                        .document(SscsDocumentDetails.builder()
                                .documentFileName(fileName)
                                .documentLink(DocumentLink.builder()
                                        .documentUrl(documentUrl)
                                        .build())
                                .documentDateAdded(convertCreatedOnDate(evidenceCreatedOn))
                                .build())
                        .questionId(questionId)
                        .build())
                .build();
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
    public void uploadsEvidenceToQuestionAndAddsItToDraftCorDocumentsInCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        final int originalNumberOfCorDocuments = sscsCaseDetails.getData().getDraftCorDocument().size();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadDraftQuestionEvidence(someOnlineHearingId, someQuestionId, file);

        assertThat(evidenceOptional.isPresent(), is(true));
        Evidence evidence = evidenceOptional.get();
        assertThat(evidence, is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
        verify(ccdService).updateCase(
                hasDraftCorDocument(originalNumberOfCorDocuments, someQuestionId, documentUrl, fileName),
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

        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadDraftQuestionEvidence(someOnlineHearingId, someQuestionId, file);

        assertThat(evidenceOptional.isPresent(), is(true));
        Evidence evidence = evidenceOptional.get();
        assertThat(evidence, is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
        verify(ccdService).updateCase(
                hasDraftCorDocument(0, someQuestionId, documentUrl, fileName),
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

        Optional<Evidence> evidence = evidenceUploadService.uploadDraftQuestionEvidence(nonExistentHearingId, someQuestionId, file);

        assertThat(evidence.isPresent(), is(false));
    }

    @Test
    public void listEvidenceWhenQuestionUnanswered() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));


        List<Evidence> evidenceList = evidenceUploadService.listQuestionEvidence(someOnlineHearingId, someQuestionId);

        assertThat(evidenceList.size(), is(1));
        assertThat(evidenceList.get(0), is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
    }

    @Test
    public void listEvidenceWhenQuestionAnswered() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        sscsCaseDetails.getData().setCorDocument(sscsCaseDetails.getData().getDraftCorDocument());
        sscsCaseDetails.getData().setDraftCorDocument(null);

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
                doesNotHaveDraftCorDocuments(),
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
        when(embedded.getDocuments()).thenReturn(singletonList(document));
        UploadResponse uploadResponse = mock(UploadResponse.class);
        when(uploadResponse.getEmbedded()).thenReturn(embedded);
        return uploadResponse;
    }

    private SscsCaseData hasDraftCorDocument(int originalNumberOfCorDocuments, String someQuestionId, String documentUrl, String fileName) {
        return argThat(argument -> {
            List<CorDocument> corDocument = argument.getDraftCorDocument();
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

    private SscsCaseData hasCorDocument(int originalNumberOfDocuments, String questionId, String documentUrl, String fileName) {
        return argThat(argument -> {
            List<CorDocument> sscsDocument = argument.getCorDocument();
            return sscsDocument.size() == originalNumberOfDocuments + 1 &&
                    sscsDocument.get(originalNumberOfDocuments).getValue().getQuestionId().equals(questionId) &&
                    sscsDocument.get(originalNumberOfDocuments).getValue().getDocument().getDocumentLink().getDocumentUrl().equals(documentUrl) &&
                    sscsDocument.get(originalNumberOfDocuments).getValue().getDocument().getDocumentFileName().equals(fileName);
        });
    }

    private SscsCaseData doesNotHaveDraftCorDocuments() {
        return argThat(argument -> {
            List<CorDocument> corDocument = argument.getDraftCorDocument();
            return corDocument.isEmpty();
        });
    }

    private SscsCaseData doesNotHaveDraftSscsDocuments() {
        return argThat(argument -> {
            List<SscsDocument> sscsDocument = argument.getDraftSscsDocument();
            return sscsDocument.isEmpty();
        });
    }

    private SscsCaseDetails createSscsCaseDetails(String questionId, String fileName, String documentUrl, Date evidenceCreatedOn) {
        return SscsCaseDetails.builder()
                .id(someCcdCaseId)
                .data(SscsCaseData.builder()
                        .draftCorDocument(singletonList(CorDocument.builder()
                                .value(CorDocumentDetails.builder()
                                        .questionId(questionId)
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
