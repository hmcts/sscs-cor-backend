package uk.gov.hmcts.reform.sscscorbackend.service.email;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence.pdf;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.domain.email.Email;
import uk.gov.hmcts.reform.sscs.domain.email.EmailAttachment;
import uk.gov.hmcts.reform.sscs.service.EmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

public class CorEmailServiceTest {

    private EmailService emailService;
    private String fromEmailAddress;
    private String dwpEmailAddress;
    private String caseworkerEmailAddress;
    private String caseReference;
    private String pdfFileName;

    @Before
    public void setUp() {
        emailService = mock(EmailService.class);
        fromEmailAddress = "from@example.com";
        dwpEmailAddress = "to@example.com";
        caseworkerEmailAddress = "caseworker@example.com";
        caseReference = "caseReference";
        pdfFileName = "pdfName.pdf";
    }

    @Test
    public void canSendEmailWithSubjectAndMessage() {
        CorEmailService corEmailService = new CorEmailService(emailService, fromEmailAddress, dwpEmailAddress, caseworkerEmailAddress);
        byte[] pdfContent = {2, 4, 6, 0, 1};
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder().caseReference(caseReference).build())
                .build();
        String message = "Some message";
        String subject = "subject";
        corEmailService.sendFileToDwp(new CohEventActionContext(pdf(pdfContent, pdfFileName), sscsCaseDetails), subject, message);

        verify(emailService).sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(dwpEmailAddress)
                .subject(subject)
                .message(message)
                .attachments(singletonList(EmailAttachment.pdf(pdfContent, pdfFileName)))
                .build());
    }

    @Test
    public void canSendEmail() {
        CorEmailService corEmailService = new CorEmailService(emailService, fromEmailAddress, dwpEmailAddress, caseworkerEmailAddress);
        String message = "Some message";
        String subject = "subject";
        corEmailService.sendEmailToDwp(subject, message);

        verify(emailService).sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(dwpEmailAddress)
                .subject(subject)
                .message(message)
                .build());
    }

    @Test
    public void canSendEmailToCaseworker() {
        CorEmailService corEmailService = new CorEmailService(emailService, fromEmailAddress, dwpEmailAddress, caseworkerEmailAddress);
        String message = "Some message";
        String subject = "subject";
        corEmailService.sendEmailToCaseworker(subject, message);

        verify(emailService).sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(caseworkerEmailAddress)
                .subject(subject)
                .message(message)
                .build());
    }
}