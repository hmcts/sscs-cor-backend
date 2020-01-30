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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
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
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;
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

@RunWith(JUnitParamsRunner.class)
public class EvidenceUploadServiceTest {

    public static final String HTTP_ANOTHER_URL = "http://anotherUrl";
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

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        ccdService = mock(CorCcdService.class);
        onlineHearingService = mock(OnlineHearingService.class);
        someOnlineHearingId = "someOnlinehearingId";
        someQuestionId = "someQuestionId";
        someEvidenceId = "someEvidenceId";

        someCcdCaseId = 123L;

        someDescription = new EvidenceDescription("some description", "idamEmail");

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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        SscsDocument evidenceDescDoc = buildSscsDocumentGivenFilename(
            "temporal unique Id ec7ae162-9834-46b7-826d-fdc9935e3187 Evidence Description -");
        sscsCaseDetails.getData().setSscsDocument(Collections.singletonList(evidenceDescDoc));
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
                and(hasSscsDocument(documentUrl, fileName, 2), doesNotHaveDraftSscsDocuments()),
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

        SscsDocument anotherFileName = buildSscsDocumentGivenFilename("anotherFileName");
        SscsDocument evidenceDescDoc = buildSscsDocumentGivenFilename(
            "temporal unique Id ec7ae162-9834-46b7-826d-fdc9935e3187 Evidence Description -");
        List<SscsDocument> list = new ArrayList<>();
        list.add(anotherFileName);
        list.add(evidenceDescDoc);
        sscsCaseDetails.getData().setSscsDocument(list);

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
                and(hasSscsDocument(documentUrl, fileName, 3), doesNotHaveDraftSscsDocuments()),
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        List<Evidence> evidenceList = evidenceUploadService.listQuestionEvidence(someOnlineHearingId, someQuestionId);

        assertThat(evidenceList.size(), is(1));
        assertThat(evidenceList.get(0), is(new Evidence(documentUrl, fileName, convertCreatedOnDate(evidenceCreatedOn))));
    }

    @Test
    public void listEvidenceWhenQuestionAnswered() {
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
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
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName, documentUrl, evidenceCreatedOn);
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
    @Parameters(method = "evidenceUploadByAppellantScenario, evidenceUploadByRepScenario")
    public void givenANonCorCaseWithScannedDocumentsAndDraftDocument_thenMoveDraftToScannedDocumentsAndUpdateCaseInCcd(
        SscsCaseDetails sscsCaseDetails, EvidenceDescription someDescription, String expectedStatementPrefix,
        String expectedEvidenceDescPrefix) {

        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));

        UploadedEvidence evidenceDescriptionPdf = mock(UploadedEvidence.class);
        when(storeEvidenceDescriptionService.storePdf(
            someCcdCaseId,
            someOnlineHearingId,
            new EvidenceDescriptionPdfData(sscsCaseDetails, someDescription,
                singletonList("someFileName.txt"))
        )).thenReturn(new CohEventActionContext(evidenceDescriptionPdf, sscsCaseDetails));

        boolean submittedEvidence = evidenceUploadService.submitHearingEvidence(someOnlineHearingId, someDescription);

        assertThat(submittedEvidence, is(true));

        verify(ccdService).updateCase(
            and(hasSscsScannedDocumentAndSscsDocuments(
                expectedStatementPrefix, expectedEvidenceDescPrefix),
                doesHaveEmptyDraftSscsDocumentsAndEvidenceHandledFlagEqualToNo()),
            eq(someCcdCaseId),
            eq(ATTACH_SCANNED_DOCS.getCcdType()),
            eq("SSCS - upload evidence from MYA"),
            eq("Uploaded a further evidence document"),
            eq(idamTokens)
        );
    }

    @Test
    public void deleteEvidenceForQuestionForAHearingThatDoesNotExist() {
        String nonExistentHearingId = "nonExistentHearingId";
        when(onlineHearingService.getCcdCase(nonExistentHearingId)).thenReturn(Optional.empty());

        boolean hearingFound = evidenceUploadService.deleteQuestionEvidence(nonExistentHearingId, someEvidenceId);

        assertThat(hearingFound, is(false));
    }

    private Object[] evidenceUploadByAppellantScenario() {
        initCommonParams();
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(someQuestionId, fileName,
            documentUrl, evidenceCreatedOn);
        sscsCaseDetails.getData().setScannedDocuments(getScannedDocuments());
        sscsCaseDetails.getData().setSscsDocument(buildSscsDocumentList());
        sscsCaseDetails.getData().setAppeal(Appeal.builder().hearingType("sya").build());
        return new Object[]{
            new Object[]{sscsCaseDetails, someDescription, "Appellant statement 1 - someFileName.txt",
                "Appellant Evidence Description -"}
        };
    }

    @NotNull
    private List<SscsDocument> buildSscsDocumentList() {
        SscsDocument descriptionDocument = buildSscsDocumentGivenFilename(
            "temporal unique Id ec7ae162-9834-46b7-826d-fdc9935e3187 Evidence Description -");
        SscsDocument form1DocWithNoDate = buildSscsDocumentGivenFilename("form1");
        form1DocWithNoDate.getValue().setDocumentDateAdded(null);
        List<SscsDocument> sscsList = new ArrayList<>();
        sscsList.add(descriptionDocument);
        sscsList.add(form1DocWithNoDate);
        return sscsList;
    }

    private SscsDocument buildSscsDocumentGivenFilename(String filename) {
        return SscsDocument.builder()
            .value(SscsDocumentDetails.builder()
                .documentFileName(filename)
                .documentLink(DocumentLink.builder()
                    .documentFilename(filename)
                    .documentUrl(HTTP_ANOTHER_URL)
                    .build())
                .documentDateAdded(convertCreatedOnDate(evidenceCreatedOn))
                .build())
            .build();
    }

    private Object[] evidenceUploadByRepScenario() {
        initCommonParams();
        SscsCaseDetails sscsCaseDetailsWithRepSubs = createSscsCaseDetails(someQuestionId, fileName,
            documentUrl, evidenceCreatedOn);
        sscsCaseDetailsWithRepSubs.getData().setSubscriptions(Subscriptions.builder()
            .representativeSubscription(Subscription.builder()
                .email("rep@email.com")
                .build())
            .build());
        sscsCaseDetailsWithRepSubs.getData().setScannedDocuments(getScannedDocuments());
        sscsCaseDetailsWithRepSubs.getData().setSscsDocument(buildSscsDocumentList());
        sscsCaseDetailsWithRepSubs.getData().setAppeal(Appeal.builder().hearingType("sya").build());
        EvidenceDescription someDescriptionWithRepEmail = new EvidenceDescription("some description",
            "rep@email.com");

        return new Object[]{
            new Object[]{sscsCaseDetailsWithRepSubs, someDescriptionWithRepEmail,
                "Representative statement 1 - someFileName.txt", "Representative Evidence Description -"}
        };
    }

    @NotNull
    private List<ScannedDocument> getScannedDocuments() {
        ScannedDocument evidenceDocument = ScannedDocument.builder()
            .value(ScannedDocumentDetails.builder()
                .fileName("anotherFileName")
                .url(DocumentLink.builder()
                    .documentUrl("http://anotherUrl")
                    .build())
                .scannedDate(convertCreatedOnDate(evidenceCreatedOn))
                .build())
            .build();
        return singletonList(evidenceDocument);
    }

    private void initCommonParams() {
        someOnlineHearingId = "someOnlinehearingId";
        someQuestionId = "someQuestionId";
        someEvidenceId = "someEvidenceId";
        someCcdCaseId = 123L;
        fileName = "someFileName.txt";
        documentUrl = "http://example.com/document/" + someEvidenceId;
        someDescription = new EvidenceDescription("some description", "idamEmail");
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

    private SscsCaseData hasSscsDocument(String documentUrl, String fileName, int expectedNumberOfDocs) {
        return argThat(argument -> {
            List<SscsDocument> sscsDocument = argument.getSscsDocument();
            boolean isExpectedStatementCount = isNumberOfDocsWithGivenNameAsExpected(documentUrl, fileName,
                sscsDocument);
            boolean isExpectedEvidenceDescCount = isNumberOfDocsWithGivenNameAsExpected(HTTP_ANOTHER_URL,
                "Evidence Description -", sscsDocument);
            return sscsDocument.size() == expectedNumberOfDocs && isExpectedStatementCount && isExpectedEvidenceDescCount;
        });
    }

    private boolean isNumberOfDocsWithGivenNameAsExpected(String documentUrl, String fileName, List<SscsDocument> sscsDocument) {
        return sscsDocument.stream()
            .filter(doc -> doc.getValue().getDocumentFileName().equals(fileName))
            .filter(doc -> doc.getValue().getDocumentLink().getDocumentUrl().equals(documentUrl))
            .count() == 1;
    }

    private SscsCaseData hasSscsScannedDocumentAndSscsDocuments(String expectedStatementPrefix,
                                                                String expectedEvidenceDescPrefix) {
        return argThat(argument -> checkSscsScannedDocument(expectedStatementPrefix, expectedEvidenceDescPrefix,
            argument.getScannedDocuments()) && checkSscsDocuments(argument.getSscsDocument()));
    }

    private boolean checkSscsDocuments(List<SscsDocument> sscsDocument) {
        boolean isExpectedNumberOfDocs = sscsDocument.size() == 1;
        return isExpectedNumberOfDocs && sscsDocument.get(0).getValue().getDocumentFileName().equals("form1");
    }

    private boolean checkSscsScannedDocument(String expectedStatementPrefix, String expectedEvidenceDescPrefix,
                                             List<ScannedDocument> scannedDocuments) {
        boolean isExpectedNumberOfScannedDocs = scannedDocuments.size() == 3;
        boolean isExpectedNumberOfAppellantStatements = scannedDocuments.stream()
            .filter(scannedDocument -> scannedDocument.getValue().getFileName().startsWith(expectedStatementPrefix))
            .count() == 1;
        boolean isExpectedNumberOfAppellantEvidenceDesc = scannedDocuments.stream()
            .filter(scannedDocument -> scannedDocument.getValue().getFileName()
                .startsWith(expectedEvidenceDescPrefix))
            .count() == 1;
        return isExpectedNumberOfAppellantEvidenceDesc && isExpectedNumberOfAppellantStatements
            && isExpectedNumberOfScannedDocs;
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

    private SscsCaseData doesHaveEmptyDraftSscsDocumentsAndEvidenceHandledFlagEqualToNo() {
        return argThat(argument -> {
            List<SscsDocument> sscsDocument = argument.getDraftSscsDocument();
            return sscsDocument.isEmpty() && argument.getEvidenceHandled().equals("No");
        });
    }

    private SscsCaseDetails createSscsCaseDetails(String questionId, String fileName, String documentUrl,
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
