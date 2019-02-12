package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Arrays.asList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.domain.email.Email;
import uk.gov.hmcts.reform.sscs.domain.email.EmailAttachment;
import uk.gov.hmcts.reform.sscs.service.EmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

@Service
public class CorEmailService {
    private final EmailService emailService;
    private final String fromEmailAddress;
    private final String dwpEmailAddress;

    public CorEmailService(
            @Autowired EmailService emailService,
            @Value("${appeal.email.from}") String fromEmailAddress,
            @Value("${appeal.email.dwpEmailAddress}") String dwpEmailAddress
    ) {
        this.emailService = emailService;
        this.fromEmailAddress = fromEmailAddress;
        this.dwpEmailAddress = dwpEmailAddress;
    }

    public void sendPdf(StorePdfResult storePdfResult, String subject, String message) {
        byte[] content = storePdfResult.getPdf().getContent();
        emailService.sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(dwpEmailAddress)
                .subject(subject)
                .message(message)
                .attachments(asList(EmailAttachment.pdf(content, storePdfResult.getPdf().getName())))
                .build());
    }

    public void sendEmail(String subject, String message) {
        emailService.sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(dwpEmailAddress)
                .subject(subject)
                .message(message)
                .build());
    }

}
