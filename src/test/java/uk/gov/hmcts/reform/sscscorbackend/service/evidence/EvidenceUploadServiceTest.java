package uk.gov.hmcts.reform.sscscorbackend.service.evidence;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.ATTACH_SCANNED_DOCS;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CorDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.CorDocumentDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.DocumentLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocumentDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocumentDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscs.service.conversion.FileToPdfConversionService;
import uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;
import uk.gov.hmcts.reform.sscscorbackend.domain.EvidenceDescription;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;
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

        storeEvidenceDescriptionService = mock(StoreEvidenceDescriptionService.class);
        evidenceUploadEmailService = mock(EvidenceUploadEmailService.class);
        FileToPdfConversionService fileToPdfConversionService = mock(FileToPdfConversionService.class);
        EvidenceManagementService evidenceManagementService = mock(EvidenceManagementService.class);
        DocumentManagementService documentManagementService = mock(DocumentManagementService.class);

        evidenceUploadService = new EvidenceUploadService(
                documentManagementService,
                ccdService,
                idamService,
                onlineHearingService,
                storeEvidenceDescriptionService,
                evidenceUploadEmailService,
            fileToPdfConversionService,
            evidenceManagementService);
        fileName = "someFileName.txt";
        documentUrl = "http://example.com/document/" + someEvidenceId;
        file = mock(MultipartFile.class);

        UploadResponse uploadResponse = createUploadResponse();
        when(evidenceManagementService.upload(singletonList(file), "sscs")).thenReturn(uploadResponse);
        when(fileToPdfConversionService.convert(singletonList(file))).thenReturn(singletonList(file));
    }

    @Test
    public void uploadsEvidenceAndAddsItToDraftSscsDocumentsInCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        final int originalNumberOfSscsDocuments = sscsCaseDetails.getData().getDraftSscsDocument().size();
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadDraftHearingEvidence(someOnlineHearingId, file);

        assertThat(evidenceOptional.isPresent(), is(true));
        Evidence evidence = evidenceOptional.get();
        assertThat(evidence, is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));

        verify(ccdService).updateCase(
                hasDraftSscsDocument(originalNumberOfSscsDocuments, documentUrl, fileName),
                eq(someCcdCaseId),
                eq("uploadDraftDocument"),
                eq("SSCS - upload document from MYA"),
                eq("Updated SSCS"),
                eq(idamTokens)
        );
    }

    @Test
    public void submitsEvidenceAddsDraftSscsDocumentsToSscsDocumentsInCcdWhenThereAreNoSscsDocuments() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
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
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));
        when(storeEvidenceDescriptionService.storePdf(
                someCcdCaseId,
                someOnlineHearingId,
                new EvidenceDescriptionPdfData(sscsCaseDetails, someDescription, singletonList(fileName))
        )).thenReturn(new CohEventActionContext(mock(UploadedEvidence.class), sscsCaseDetails));

        String questionHeader = "question header";
        List<CorDocument> draftCorDocument = sscsCaseDetails.getData().getDraftCorDocument();

        Question question = createQuestion(questionHeader);
        boolean submittedEvidence = evidenceUploadService.submitQuestionEvidence(someOnlineHearingId, question);

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

    private Question createQuestion(String questionHeader) {
        return new Question(someOnlineHearingId, someQuestionId, 1, questionHeader, "questionBody", "answerId", "someAnswer", AnswerState.draft, "2018-10-10", emptyList());
    }

    @Test
    public void submitsQuestionEvidenceAddsDraftCorDocumentsToCorDocumentsInCcdWhenThereAreOtherCorDocuments() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        List<CorDocument> list = singletonList(createCorDocument(fileName, documentUrl, someQuestionId));
        sscsCaseDetails.getData().setCorDocument(list);

        final int originalNumberOfSscsDocuments = sscsCaseDetails.getData().getCorDocument().size();
        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        String questionHeader = "question header";
        List<CorDocument> draftCorDocument = sscsCaseDetails.getData().getDraftCorDocument();

        Question question = createQuestion(questionHeader);
        boolean submittedEvidence = evidenceUploadService.submitQuestionEvidence(someOnlineHearingId, question);

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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        String draftDocumentUrl = "http://url1";
        String draftFileName = "file1";
        String expectedFileName = "file2";
        String expectedUrl = "http://url2";
        String draftQuestionId = "1";
        CorDocument expectedCorDocument = createCorDocument(expectedFileName, expectedUrl, someQuestionId);
        List<CorDocument> draftCorDocuments = asList(
                createCorDocument(draftFileName, draftDocumentUrl, draftQuestionId),
                expectedCorDocument
        );
        sscsCaseDetails.getData().setDraftCorDocument(draftCorDocuments);

        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));
        String questionHeader = "question header";
        Question question = createQuestion(questionHeader);

        boolean submittedEvidence = evidenceUploadService.submitQuestionEvidence(someOnlineHearingId, question);

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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        sscsCaseDetails.getData().setDraftCorDocument(null);
        sscsCaseDetails.getData().setCorDocument(null);

        when(onlineHearingService.getCcdCase(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));
        String questionHeader = "question header";
        Question question = createQuestion(questionHeader);

        boolean submittedEvidence = evidenceUploadService.submitQuestionEvidence(someOnlineHearingId, question);

        assertThat(submittedEvidence, is(true));
        verifyZeroInteractions(evidenceUploadEmailService);
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
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        Optional<Evidence> evidenceOptional = evidenceUploadService.uploadDraftHearingEvidence(someOnlineHearingId, file);

        assertThat(evidenceOptional.isPresent(), is(true));
        Evidence evidence = evidenceOptional.get();
        assertThat(evidence, is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
        verify(ccdService).updateCase(
            hasDraftSscsDocument(0, documentUrl, fileName),
            eq(someCcdCaseId),
            eq("uploadDraftDocument"),
            eq("SSCS - upload document from MYA"),
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        final int originalNumberOfCorDocuments = sscsCaseDetails.getData().getDraftCorDocument().size();
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

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
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        List<Evidence> evidenceList = evidenceUploadService.listQuestionEvidence(someOnlineHearingId, someQuestionId);

        assertThat(evidenceList.size(), is(1));
        assertThat(evidenceList.get(0), is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
    }

    @Test
    public void listEvidenceWhenQuestionAnswered() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        sscsCaseDetails.getData().setCorDocument(sscsCaseDetails.getData().getDraftCorDocument());
        sscsCaseDetails.getData().setDraftCorDocument(null);

        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));


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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc("someOtherQuestionId", fileName, documentUrl, evidenceCreatedOn);
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

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
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

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
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

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

    @Test
    public void givenANonCorCaseWithScannedDocumentsAndDraftDocument_thenMoveDraftToScannedDocumentsAndUpdateCaseInCcd() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDraftSscsDocOneDraftCorDocAndOneScannedDoc();
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId))
            .thenReturn(Optional.of(sscsCaseDetails));

        when(storeEvidenceDescriptionService.storePdf(
            someCcdCaseId,
            someOnlineHearingId,
            new EvidenceDescriptionPdfData(sscsCaseDetails, someDescription, singletonList(fileName))
        )).thenReturn(new CohEventActionContext(mock(UploadedEvidence.class), sscsCaseDetails));

        boolean submittedEvidence = evidenceUploadService.submitHearingEvidence(someOnlineHearingId, someDescription);

        assertThat(submittedEvidence, is(true));

        verify(ccdService).updateCase(
            and(hasSscsScannedDocument(), doesNotHaveDraftSscsDocumentsAndEvidenceHandledNo()),
            eq(someCcdCaseId),
            eq(ATTACH_SCANNED_DOCS.getCcdType()),
            eq("SSCS - upload evidence from MYA"),
            eq("Uploaded a further evidence document"),
            eq(idamTokens)
        );
    }

    @NotNull
    private SscsCaseDetails createSscsCaseDetailsWithOneDraftSscsDocOneDraftCorDocAndOneScannedDoc() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetailsWithOneDocOfEachType(someQuestionId,
            fileName, documentUrl, evidenceCreatedOn);
        ScannedDocument existingScannedDocument = ScannedDocument.builder()
            .value(ScannedDocumentDetails.builder()
                .fileName("anotherFileName")
                .url(DocumentLink.builder()
                    .documentUrl("http://anotherUrl")
                    .build())
                .scannedDate(convertCreatedOnDate(evidenceCreatedOn))
                .build())
            .build();
        sscsCaseDetails.getData().setScannedDocuments(singletonList(existingScannedDocument));
        sscsCaseDetails.getData().setAppeal(Appeal.builder().hearingType("sya").build());
        return sscsCaseDetails;
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

    private SscsCaseData hasSscsScannedDocument() {
        return argThat(argument -> {
            List<ScannedDocument> scannedDocuments = argument.getScannedDocuments();
            List<ScannedDocument> newAddedScannedDocs = scannedDocuments.stream()
                .filter(doc -> doc.getValue().getUrl().getDocumentUrl().equals(documentUrl))
                .filter(doc -> doc.getValue().getFileName().equals(fileName))
                .collect(Collectors.toList());

            return scannedDocuments.size() == 2 && newAddedScannedDocs.size() == 1;
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

    private SscsCaseData doesNotHaveDraftSscsDocumentsAndEvidenceHandledNo() {
        return argThat(argument -> {
            List<SscsDocument> sscsDocument = argument.getDraftSscsDocument();
            return sscsDocument.isEmpty() && argument.getEvidenceHandled().equals("No");
        });
    }

    private SscsCaseDetails createSscsCaseDetailsWithOneDraftSscsDocAndDraftCorSscsDoc(String questionId,
                                                                                       String fileName,
                                                                                       String documentUrl,
                                                                                       Date evidenceCreatedOn) {
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

    private SscsCaseDetails createSscsCaseDetailsWithOneDocOfEachType(String questionId, String fileName,
                                                                      String documentUrl, Date evidenceCreatedOn) {

        List<SscsDocument> sscsDocuments = new ArrayList<>(1);
        sscsDocuments.add(SscsDocument.builder()
            .value(SscsDocumentDetails.builder()
                .documentFileName(fileName)
                .documentLink(DocumentLink.builder()
                    .documentUrl(documentUrl)
                    .build())
                .documentDateAdded(convertCreatedOnDate(evidenceCreatedOn))
                .build())
            .build());

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
                .draftSscsDocument(sscsDocuments)
                .sscsDocument(sscsDocuments)
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
