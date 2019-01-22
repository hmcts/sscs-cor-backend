package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.domain.email.Email;
import uk.gov.hmcts.reform.sscs.domain.email.EmailAttachment;
import uk.gov.hmcts.reform.sscs.service.EmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.Pdf;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

public class CorEmailServiceTest {

    private EmailService emailService;
    private String fromEmailAddress;
    private String toEmailAddress;
    private String caseReference;
    private String pdfFileName;
    private QuestionsEmailMessageBuilder questionsEmailMessageBuilder;

    @Before
    public void setUp() {
        emailService = mock(EmailService.class);
        fromEmailAddress = "from@example.com";
        toEmailAddress = "to@example.com";
        caseReference = "caseReference";
        pdfFileName = "pdfName.pdf";
        questionsEmailMessageBuilder = mock(QuestionsEmailMessageBuilder.class);
    }

    @Test
    public void canSendEmail() {
        CorEmailService corEmailService = new CorEmailService(emailService, questionsEmailMessageBuilder, fromEmailAddress, toEmailAddress);
        byte[] pdfContent = {2, 4, 6, 0, 1};
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder().caseReference(caseReference).build())
                .build();
        String message = "Some message";
        when(questionsEmailMessageBuilder.getMessage(sscsCaseDetails)).thenReturn(message);
        corEmailService.sendPdf(new StorePdfResult(new Pdf(pdfContent, pdfFileName), sscsCaseDetails));

        verify(emailService).sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(toEmailAddress)
                .subject("Questions issued to the appellant (" + caseReference + ")")
                .message(message)
                .attachments(singletonList(EmailAttachment.pdf(pdfContent, pdfFileName)))
                .build());
    }
}