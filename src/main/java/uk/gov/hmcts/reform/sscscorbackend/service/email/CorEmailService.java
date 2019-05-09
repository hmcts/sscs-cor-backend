package uk.gov.hmcts.reform.sscscorbackend.service.email;

import static java.util.Arrays.asList;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.domain.email.Email;
import uk.gov.hmcts.reform.sscs.domain.email.EmailAttachment;
import uk.gov.hmcts.reform.sscs.service.EmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence;

@Slf4j
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

    @Deprecated // use method below that takes a UploadedEvidence object
    public void sendFileToDwp(CohEventActionContext cohEventActionContext, String subject, String message) {
        sendFileToDwp(cohEventActionContext.getPdf(), subject, message);
    }

    public void sendFileToDwp(UploadedEvidence pdf, String subject, String message) {
        log.info("Sending email and PDf with subject [" + subject + "] to DWP");
        emailService.sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(dwpEmailAddress)
                .subject(subject)
                .message(message)
                .attachments(asList(EmailAttachment.builder()
                        .contentType(pdf.getContentType())
                        .data(pdf.getContent())
                        .filename(pdf.getName())
                        .build()
                ))
                .build());
    }

    public void sendEmailToDwp(String subject, String message) {
        log.info("Sending email with subject [" + subject + "] to DWP");
        emailService.sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(dwpEmailAddress)
                .subject(subject)
                .message(message)
                .build());
    }

    public void sendEmailToCaseworker(String subject, String message) {
        log.info("Sending email with subject [" + subject + "] to caseworker");
        emailService.sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(fromEmailAddress)
                .subject(subject)
                .message(message)
                .build());
    }
}
