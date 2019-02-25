package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static org.mockito.Mockito.*;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreAnswersPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.Pdf;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

public class AnswerSubmittedEventActionTest {
    @Test
    public void canSendPdf() {
        CorEmailService corEmailService = mock(CorEmailService.class);
        StoreAnswersPdfService storeAnswersPdfService = mock(StoreAnswersPdfService.class);
        DwpEmailMessageBuilder dwpEmailMessageBuilder = mock(DwpEmailMessageBuilder.class);

        AnswerSubmittedEventAction answerSubmittedEventAction = new AnswerSubmittedEventAction(corEmailService, storeAnswersPdfService, dwpEmailMessageBuilder);

        String someCaseReference = "someCaseReference";
        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder().caseReference(someCaseReference).build())
                .build();
        long caseId = 123L;
        String onlineHearingId = "onlineHearingId";
        String pdfName = "pdf_name.pdf";
        StorePdfResult storePdfResult = new StorePdfResult(new Pdf(new byte[]{2, 5, 6, 0, 1}, pdfName), caseDetails);
        when(dwpEmailMessageBuilder.getAnswerMessage(caseDetails)).thenReturn("some message");

        answerSubmittedEventAction.handle(caseId, onlineHearingId, storePdfResult);

        verify(corEmailService).sendPdfToDwp(storePdfResult, "Appellant has provided information (" + someCaseReference + ")", "some message");
    }
}