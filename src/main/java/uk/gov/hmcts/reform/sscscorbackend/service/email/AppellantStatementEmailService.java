package uk.gov.hmcts.reform.sscscorbackend.service.email;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

@Service
public class AppellantStatementEmailService {
    private final CorEmailService corEmailService;
    private final EmailMessageBuilder emailMessageBuilder;

    public AppellantStatementEmailService(CorEmailService corEmailService, EmailMessageBuilder emailMessageBuilder) {
        this.corEmailService = corEmailService;
        this.emailMessageBuilder = emailMessageBuilder;
    }

    public void sendEmail(CohEventActionContext cohEventActionContext) {
        String message = emailMessageBuilder.getAppellantStatementMessage(cohEventActionContext.getDocument());

        String subject = "COR: Additional evidence submitted (" + cohEventActionContext.getDocument().getData().getCaseReference() + ")";
        corEmailService.sendPdfToDwp(cohEventActionContext, subject, message);
    }
}
