package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static org.mockito.Mockito.*;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.service.AnswersEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreAnswersPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.Pdf;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

public class AnswerSubmittedEventActionTest {
    @Test
    public void canSendPdf() {
        CorEmailService corEmailService = mock(CorEmailService.class);
        StoreAnswersPdfService storeAnswersPdfService = mock(StoreAnswersPdfService.class);
        AnswersEmailMessageBuilder answersEmailMessageBuilder = mock(AnswersEmailMessageBuilder.class);

        AnswerSubmittedEventAction answerSubmittedEventAction = new AnswerSubmittedEventAction(corEmailService, storeAnswersPdfService, answersEmailMessageBuilder);

        String someCaseReference = "someCaseReference";
        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder().caseReference(someCaseReference).build())
                .build();
        long caseId = 123L;
        String onlineHearingId = "onlineHearingId";
        String pdfName = "pdf_name.pdf";
        StorePdfResult storePdfResult = new StorePdfResult(new Pdf(new byte[]{2, 5, 6, 0, 1}, pdfName), caseDetails);
        when(storeAnswersPdfService.storePdf(caseId, onlineHearingId)).thenReturn(storePdfResult);
        when(answersEmailMessageBuilder.getMessage(caseDetails)).thenReturn("some message");

        answerSubmittedEventAction.handle(caseId, onlineHearingId, DataFixtures.someCohEvent(caseId + "", onlineHearingId, "some_event"));

        verify(corEmailService).sendPdf(storePdfResult, "Appellant has provided information (" + someCaseReference + ")", "some message");
    }

}