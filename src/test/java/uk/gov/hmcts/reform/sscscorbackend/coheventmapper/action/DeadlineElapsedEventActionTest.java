package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.action;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someQuestionRound;
import static uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence.pdf;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions.DeadlineElapsedEventAction;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.evidence.EvidenceUploadEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreAnswersDeadlineElapsedPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;

public class DeadlineElapsedEventActionTest {
    @Test
    public void canSendPdf() {
        CorEmailService corEmailService = mock(CorEmailService.class);
        StoreAnswersDeadlineElapsedPdfService storeAnswersPdfService = mock(StoreAnswersDeadlineElapsedPdfService.class);
        EmailMessageBuilder emailMessageBuilder = mock(EmailMessageBuilder.class);
        EvidenceUploadEmailService evidenceUploadEmailService = mock(EvidenceUploadEmailService.class);
        QuestionService questionService = mock(QuestionService.class);

        DeadlineElapsedEventAction deadlineElapsedEventAction = new DeadlineElapsedEventAction(corEmailService, storeAnswersPdfService, emailMessageBuilder, evidenceUploadEmailService, questionService);

        String someCaseReference = "someCaseReference";
        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder().caseReference(someCaseReference).build())
                .build();
        long caseId = 123L;
        String onlineHearingId = "onlineHearingId";
        String pdfName = "pdf_name.pdf";
        CohEventActionContext cohEventActionContext = new CohEventActionContext(pdf(new byte[]{2, 5, 6, 0, 1}, pdfName), caseDetails);
        when(emailMessageBuilder.getAnswerMessage(caseDetails)).thenReturn("some message");
        when(storeAnswersPdfService.storePdf(caseId, onlineHearingId, new PdfData(caseDetails)))
                .thenReturn(cohEventActionContext);
        QuestionRound questionRound = someQuestionRound();
        when(questionService.getQuestions(onlineHearingId, true)).thenReturn(questionRound);

        CohEventActionContext result = deadlineElapsedEventAction.handle(caseId, onlineHearingId, caseDetails);

        verify(corEmailService).sendFileToDwp(cohEventActionContext, "Appellant has provided information (" + someCaseReference + ")", "some message");
        verify(evidenceUploadEmailService).sendQuestionEvidenceToDwp(questionRound.getQuestions(), caseDetails);
        assertThat(result, is(cohEventActionContext));
    }
}