package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.action;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions.DeadlineElapsedEventAction;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreAnswersDeadlineElapsedPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.Pdf;

public class DeadlineElapsedEventActionTest {
    @Test
    public void canSendPdf() {
        CorEmailService corEmailService = mock(CorEmailService.class);
        StoreAnswersDeadlineElapsedPdfService storeAnswersPdfService = mock(StoreAnswersDeadlineElapsedPdfService.class);
        EmailMessageBuilder emailMessageBuilder = mock(EmailMessageBuilder.class);

        DeadlineElapsedEventAction deadlineElapsedEventAction = new DeadlineElapsedEventAction(corEmailService, storeAnswersPdfService, emailMessageBuilder);

        String someCaseReference = "someCaseReference";
        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder().caseReference(someCaseReference).build())
                .build();
        long caseId = 123L;
        String onlineHearingId = "onlineHearingId";
        String pdfName = "pdf_name.pdf";
        CohEventActionContext cohEventActionContext = new CohEventActionContext(new Pdf(new byte[]{2, 5, 6, 0, 1}, pdfName), caseDetails);
        when(emailMessageBuilder.getAnswerMessage(caseDetails)).thenReturn("some message");

        CohEventActionContext result = deadlineElapsedEventAction.handle(caseId, onlineHearingId, cohEventActionContext);

        verify(corEmailService).sendPdfToDwp(cohEventActionContext, "Appellant has provided information (" + someCaseReference + ")", "some message");
        assertThat(result, is(cohEventActionContext));
    }
}