package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someQuestionRound;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

public class SendToDwpServiceTest {

    private QuestionService questionService;
    private PdfService pdfService;
    private String someOnlineHearingId;
    private DwpEmailService dwpEmailService;

    @Before
    public void setUp() {
        questionService = mock(QuestionService.class);
        pdfService = mock(PdfService.class);
        someOnlineHearingId = "some_online_hearing_id";
        dwpEmailService = mock(DwpEmailService.class);
    }

    @Test
    public void sendQuestionsToDwp() {
        QuestionRound questionRound = someQuestionRound();
        when(questionService.getQuestions(someOnlineHearingId)).thenReturn(questionRound);
        byte[] questionsPdf = {2, 4, 6, 0, 1};
        when(pdfService.createPdf(questionRound)).thenReturn(questionsPdf);

        SendToDwpService sendToDwpService = new SendToDwpService(questionService, pdfService, dwpEmailService);
        sendToDwpService.sendToDwp(someOnlineHearingId);

        verify(dwpEmailService).send(questionsPdf);
    }
}