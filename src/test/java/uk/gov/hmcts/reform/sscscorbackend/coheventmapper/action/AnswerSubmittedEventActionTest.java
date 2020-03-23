package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.action;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.draft;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.submitted;
import static uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence.pdf;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions.AnswerSubmittedEventAction;
import uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionSummary;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.evidence.EvidenceUploadEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreAnswersPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;

public class AnswerSubmittedEventActionTest {

    private CorEmailService corEmailService;
    private StoreAnswersPdfService storeAnswersPdfService;
    private EmailMessageBuilder emailMessageBuilder;
    private AnswerSubmittedEventAction answerSubmittedEventAction;
    private long caseId;
    private String onlineHearingId;
    private SscsCaseDetails caseDetails;
    private String someCaseReference;
    private QuestionService questionService;
    private EvidenceUploadEmailService evidenceUploadEmailService;

    @Before
    public void setUp() throws Exception {
        corEmailService = mock(CorEmailService.class);
        storeAnswersPdfService = mock(StoreAnswersPdfService.class);
        emailMessageBuilder = mock(EmailMessageBuilder.class);
        questionService = mock(QuestionService.class);
        evidenceUploadEmailService = mock(EvidenceUploadEmailService.class);

        answerSubmittedEventAction = new AnswerSubmittedEventAction(corEmailService, storeAnswersPdfService, emailMessageBuilder, evidenceUploadEmailService, questionService);
        caseId = 123L;
        onlineHearingId = "onlineHearingId";

        someCaseReference = "someCaseReference";
        caseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder().caseReference(someCaseReference).build())
                .build();
    }

    @Test
    public void canSendPdf() {
        QuestionRound questionRoundWithAllQuestionsAnswered = getQuestionRound(submitted);
        when(questionService.getQuestions(onlineHearingId, true)).thenReturn(questionRoundWithAllQuestionsAnswered);

        String pdfName = "pdf_name.pdf";
        CohEventActionContext cohEventActionContext = new CohEventActionContext(pdf(new byte[]{2, 5, 6, 0, 1}, pdfName), caseDetails);
        when(emailMessageBuilder.getAnswerMessage(caseDetails)).thenReturn("some message");
        when(storeAnswersPdfService.storePdf(caseId, onlineHearingId, new PdfData(caseDetails)))
                .thenReturn(cohEventActionContext);

        CohEventActionContext result = answerSubmittedEventAction.handle(caseId, onlineHearingId, caseDetails);

        verify(corEmailService).sendFileToDwp(cohEventActionContext, "Appellant has provided information (" + someCaseReference + ")", "some message", caseDetails.getId());
        assertThat(result, is(cohEventActionContext));
    }

    @Test
    public void createsPdfIfAllQuestionsAnswered() {
        QuestionRound questionRoundWithAllQuestionsAnswered = getQuestionRound(submitted);
        when(questionService.getQuestions(onlineHearingId, true)).thenReturn(questionRoundWithAllQuestionsAnswered);

        answerSubmittedEventAction.handle(caseId, onlineHearingId, caseDetails);

        verify(storeAnswersPdfService).storePdf(eq(caseId), eq(onlineHearingId), any(PdfData.class));
    }

    @Test
    public void doesNotCreatePdfIfNotAllQuestionsAnswered() {
        QuestionRound questionRoundWithSomeUnansweredQuestions = getQuestionRound(draft);
        when(questionService.getQuestions(onlineHearingId, true)).thenReturn(questionRoundWithSomeUnansweredQuestions);
        answerSubmittedEventAction.handle(caseId, onlineHearingId, caseDetails);

        verifyZeroInteractions(storeAnswersPdfService);
    }

    private QuestionRound getQuestionRound(AnswerState answerState) {
        List<QuestionSummary> questionSummaries =
                asList(new QuestionSummary("someQuestionId", 1, "someQuestionHeader", "someQuestionBody", submitted, "2018-08-08T09:12:12Z", "someAnswer"),
                        new QuestionSummary("someQuestionId", 2, "someQuestionHeader", "someQuestionBody", answerState, null,"someAnswer"));
        return new QuestionRound(questionSummaries, now().plusDays(7).format(ISO_LOCAL_DATE_TIME), 0);
    }
}