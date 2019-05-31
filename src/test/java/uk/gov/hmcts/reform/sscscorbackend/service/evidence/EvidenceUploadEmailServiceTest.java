package uk.gov.hmcts.reform.sscscorbackend.service.evidence;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.draft;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.submitted;
import static uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence.pdf;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionSummary;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence;

public class EvidenceUploadEmailServiceTest {

    private CorEmailService corEmailService;
    private SscsCaseDetails sscsCaseDetails;
    private String caseReference;
    private EmailMessageBuilder emailMessageBuilder;
    private FileDownloader fileDownloader;
    private UploadedEvidence evidenceDescriptionPdf;
    private EvidenceUploadEmailService evidenceUploadEmailService;

    @Before
    public void setUp() {
        corEmailService = mock(CorEmailService.class);
        caseReference = "caseReference";
        sscsCaseDetails = SscsCaseDetails.builder().data(SscsCaseData.builder().caseReference(caseReference).build()).build();
        emailMessageBuilder = mock(EmailMessageBuilder.class);
        fileDownloader = mock(FileDownloader.class);
        evidenceDescriptionPdf = pdf(new byte[]{2, 4, 6, 0, 1}, "Evidence description");

        evidenceUploadEmailService = new EvidenceUploadEmailService(corEmailService, emailMessageBuilder, fileDownloader);
    }

    @Test
    public void sendsEvidenceToDwp() {
        String message = "some message";
        String docUrl = "docUrl";
        String fileName = "fileName";
        SscsDocument sscsDocument = SscsDocument.builder()
                .value(SscsDocumentDetails.builder()
                        .documentFileName(fileName)
                        .documentLink(DocumentLink.builder()
                                .documentBinaryUrl(docUrl)
                                .build())
                        .build())
                .build();

        when(emailMessageBuilder.getEvidenceSubmittedMessage(sscsCaseDetails)).thenReturn(message);
        UploadedEvidence expectedFile = mock(UploadedEvidence.class);
        when(fileDownloader.downloadFile(docUrl)).thenReturn(expectedFile);

        evidenceUploadEmailService.sendToDwp(
                evidenceDescriptionPdf,
                singletonList(sscsDocument),
                sscsCaseDetails
        );

        verify(corEmailService, times(1)).sendFileToDwp(
                evidenceDescriptionPdf,
                "Evidence uploaded (" + caseReference + ")",
                message
        );

        verify(fileDownloader).downloadFile(docUrl);
        verify(corEmailService, times(1)).sendFileToDwp(
                eq(expectedFile),
                eq("Evidence uploaded (" + caseReference + ")"),
                eq(message)
        );
    }

    @Test
    public void sendsQuestionEvidenceToDwp() {
        String message = "some message";
        String docUrl = "docUrl";
        String fileName = "fileName";

        CorDocument corDocument = createCorDocument(docUrl, fileName, "someQuestionId");

        QuestionSummary question = DataFixtures.someQuestionSummary();

        when(emailMessageBuilder.getQuestionEvidenceSubmittedMessage(sscsCaseDetails, question)).thenReturn(message);
        UploadedEvidence expectedFile = mock(UploadedEvidence.class);
        when(fileDownloader.downloadFile(docUrl)).thenReturn(expectedFile);

        evidenceUploadEmailService.sendToDwp(
                question,
                singletonList(corDocument),
                sscsCaseDetails
        );

        verify(fileDownloader).downloadFile(docUrl);
        verify(corEmailService, times(1)).sendFileToDwp(
                eq(expectedFile),
                eq("Evidence uploaded (" + caseReference + ")"),
                eq(message)
        );
    }

    @Test
    public void sendsAllQuestionsEvidence() {
        String docUrl1 = "docUrl1";
        String questionId1 = "someQuestionId1";
        String questionId2 = "someQuestionId2";
        String questionIdWithNoEvidence = "questionIdWithNoEvidence";
        List<QuestionSummary> questionSummaries = asList(
                new QuestionSummary(questionId1, 1, "someQuestionHeader", "someQuestionBody", submitted, "2018-08-08T09:12:12Z", "someAnswer"),
                new QuestionSummary(questionId2, 2, "someQuestionHeader", "someQuestionBody", submitted, "2018-08-08T09:12:12Z", "someAnswer"),
                new QuestionSummary(questionIdWithNoEvidence, 3, "someQuestionHeader", "someQuestionBody", submitted, "2018-08-08T09:12:12Z", "someAnswer")
        );
        CorDocument corDocument1 = createCorDocument(docUrl1, "fileName1", questionId1);

        String docUrl2 = "docUrl2";
        CorDocument corDocument2 = createCorDocument(docUrl2, "fileName2", questionId2);

        List<CorDocument> corDocuments = asList(corDocument1, corDocument2);
        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(corDocuments);

        UploadedEvidence expectedFile = mock(UploadedEvidence.class);
        when(fileDownloader.downloadFile(docUrl1)).thenReturn(expectedFile);
        UploadedEvidence expectedFile2 = mock(UploadedEvidence.class);
        when(fileDownloader.downloadFile(docUrl2)).thenReturn(expectedFile2);
        String message1 = "some message1";
        when(emailMessageBuilder.getQuestionEvidenceSubmittedMessage(sscsCaseDetails, questionSummaries.get(0))).thenReturn(message1);
        String message2 = "some message2";
        when(emailMessageBuilder.getQuestionEvidenceSubmittedMessage(sscsCaseDetails, questionSummaries.get(1))).thenReturn(message2);

        evidenceUploadEmailService.sendQuestionEvidenceToDwp(questionSummaries, sscsCaseDetails);

        verify(fileDownloader).downloadFile(docUrl1);
        verify(fileDownloader).downloadFile(docUrl2);
        verify(corEmailService, times(1)).sendFileToDwp(
                eq(expectedFile),
                eq("Evidence uploaded (" + caseReference + ")"),
                eq(message1)
        );
        verify(corEmailService, times(1)).sendFileToDwp(
                eq(expectedFile2),
                eq("Evidence uploaded (" + caseReference + ")"),
                eq(message2)
        );
    }

    @Test
    public void doesNotSendUnsubmittedQuestions() {
        String questionId1 = "someQuestionId1";

        List<QuestionSummary> questionSummaries = singletonList(
                new QuestionSummary(questionId1, 1, "someQuestionHeader", "someQuestionBody", draft, "2018-08-08T09:12:12Z", "someAnswer")
        );

        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(singletonList(createCorDocument("docUrl1", "fileName1", questionId1)));


        evidenceUploadEmailService.sendQuestionEvidenceToDwp(questionSummaries, sscsCaseDetails);

        verifyZeroInteractions(fileDownloader);
        verifyZeroInteractions(corEmailService);
    }

    @Test
    public void canHandleNoEvidenceToSendToDwp() {
        String questionId1 = "someQuestionId1";

        List<QuestionSummary> questionSummaries = singletonList(
                new QuestionSummary(questionId1, 1, "someQuestionHeader", "someQuestionBody", submitted, "2018-08-08T09:12:12Z", "someAnswer")
        );

        SscsCaseDetails sscsCaseDetails = createSscsCaseDetails(null);


        evidenceUploadEmailService.sendQuestionEvidenceToDwp(questionSummaries, sscsCaseDetails);

        verifyZeroInteractions(fileDownloader);
        verifyZeroInteractions(corEmailService);
    }

    private SscsCaseDetails createSscsCaseDetails(List<CorDocument> corDocument) {
        return SscsCaseDetails.builder().data(SscsCaseData.builder()
                .caseReference(caseReference)
                .corDocument(corDocument)
                .build()
        ).build();
    }

    private CorDocument createCorDocument(String docUrl1, String fileName, String questionId1) {
        return CorDocument.builder()
                .value(CorDocumentDetails.builder()
                        .questionId(questionId1)
                        .document(SscsDocumentDetails.builder()
                                .documentFileName(fileName)
                                .documentLink(DocumentLink.builder()
                                        .documentBinaryUrl(docUrl1)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}