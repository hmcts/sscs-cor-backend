package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

public class SendToDwpService {
    private final QuestionService questionService;
    private final PdfService pdfService;
    private final DwpEmailService dwpEmailService;

    public SendToDwpService(
            QuestionService questionService,
            @Qualifier("QuestionPdfService") PdfService pdfService,
            DwpEmailService dwpEmailService) {
        this.questionService = questionService;
        this.pdfService = pdfService;
        this.dwpEmailService = dwpEmailService;
    }

    public void sendToDwp(String onlineHearingId) {
        QuestionRound questions = questionService.getQuestions(onlineHearingId);
        byte[] pdf = pdfService.createPdf(questions);
        dwpEmailService.send(pdf);
    }
}
