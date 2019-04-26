package uk.gov.hmcts.reform.sscscorbackend.service.email;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.domain.email.Email;
import uk.gov.hmcts.reform.sscs.domain.email.EmailAttachment;
import uk.gov.hmcts.reform.sscs.service.EmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.Pdf;

public class CorEmailServiceTest {

    private EmailService emailService;
    private String fromEmailAddress;
    private String toEmailAddress;
    private String caseReference;
    private String pdfFileName;

    @Before
    public void setUp() {
        emailService = mock(EmailService.class);
        fromEmailAddress = "from@example.com";
        toEmailAddress = "to@example.com";
        caseReference = "caseReference";
        pdfFileName = "pdfName.pdf";
    }

    @Test
    public void canSendEmailWithSubjectAndMessage() {
        CorEmailService corEmailService = new CorEmailService(emailService, fromEmailAddress, toEmailAddress);
        byte[] pdfContent = {2, 4, 6, 0, 1};
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder().caseReference(caseReference).build())
                .build();
        String message = "Some message";
        String subject = "subject";
        corEmailService.sendPdfToDwp(new CohEventActionContext(new Pdf(pdfContent, pdfFileName), sscsCaseDetails), subject, message);

        verify(emailService).sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(toEmailAddress)
                .subject(subject)
                .message(message)
                .attachments(singletonList(EmailAttachment.pdf(pdfContent, pdfFileName)))
                .build());
    }

    @Test
    public void canSendEmail() {
        CorEmailService corEmailService = new CorEmailService(emailService, fromEmailAddress, toEmailAddress);
        String message = "Some message";
        String subject = "subject";
        corEmailService.sendEmailToDwp(subject, message);

        verify(emailService).sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(toEmailAddress)
                .subject(subject)
                .message(message)
                .build());
    }
}