package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

@Service
public class DecisionAcceptedEmailService {
    private final CorEmailService corEmailService;
    private final DwpEmailMessageBuilder dwpEmailMessageBuilder;

    public DecisionAcceptedEmailService(
            CorEmailService corEmailService,
            DwpEmailMessageBuilder dwpEmailMessageBuilder
    ) {
        this.corEmailService = corEmailService;
        this.dwpEmailMessageBuilder = dwpEmailMessageBuilder;
    }

    public void sendEmail(SscsCaseDetails caseDetails) {
        String decisionIssuedMessage = dwpEmailMessageBuilder.getDecisionAcceptedMessage(caseDetails);
        String subject = "Tribunal view accepted (" + caseDetails.getData().getCaseReference() + ")";
        corEmailService.sendEmailToCaseworker(subject, decisionIssuedMessage);
        corEmailService.sendEmailToDwp(subject, decisionIssuedMessage);
    }
}
