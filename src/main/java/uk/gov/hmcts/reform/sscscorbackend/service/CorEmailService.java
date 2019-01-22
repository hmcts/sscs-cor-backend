package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Arrays.asList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.domain.email.Email;
import uk.gov.hmcts.reform.sscs.domain.email.EmailAttachment;
import uk.gov.hmcts.reform.sscs.service.EmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

@Service
public class CorEmailService {
    private final EmailService emailService;
    private final QuestionsEmailMessageBuilder questionsEmailMessageBuilder;
    private final String fromEmailAddress;
    private final String dwpEmailAddress;

    public CorEmailService(
            @Autowired EmailService emailService,
            @Autowired QuestionsEmailMessageBuilder questionsEmailMessageBuilder,
            @Value("${appeal.email.from}")String fromEmailAddress,
            @Value("${appeal.email.dwpEmailAddress}") String dwpEmailAddress
    ) {
        this.emailService = emailService;
        this.questionsEmailMessageBuilder = questionsEmailMessageBuilder;
        this.fromEmailAddress = fromEmailAddress;
        this.dwpEmailAddress = dwpEmailAddress;
    }

    public void sendPdf(StorePdfResult storePdfResult) {
        byte[] content = storePdfResult.getPdf().getContent();
        SscsCaseDetails sscsCaseDetails = storePdfResult.getDocument();
        String caseReference = sscsCaseDetails.getData().getCaseReference();
        emailService.sendEmail(Email.builder()
                .from(fromEmailAddress)
                .to(dwpEmailAddress)
                .subject("Questions issued to the appellant (" + caseReference + ")")
                .message(questionsEmailMessageBuilder.getMessage(sscsCaseDetails))
                .attachments(asList(EmailAttachment.pdf(content, storePdfResult.getPdf().getName())))
                .build());
    }

}
