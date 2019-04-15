package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.TribunalViewResponse;

@Service
public class DecisionEmailService {
    private final CorEmailService corEmailService;
    private final EmailMessageBuilder emailMessageBuilder;
    private final JuiUrlGenerator juiUrlGenerator;

    public DecisionEmailService(
            CorEmailService corEmailService,
            EmailMessageBuilder emailMessageBuilder,
            JuiUrlGenerator juiUrlGenerator) {
        this.corEmailService = corEmailService;
        this.emailMessageBuilder = emailMessageBuilder;
        this.juiUrlGenerator = juiUrlGenerator;
    }

    public void sendEmail(SscsCaseDetails caseDetails, TribunalViewResponse tribunalViewResponse) {
        if (tribunalViewResponse.getReply().equals("decision_accepted")) {
            String decisionIssuedMessage = emailMessageBuilder.getDecisionAcceptedMessage(caseDetails);
            String subject = "Tribunal view accepted (" + caseDetails.getData().getCaseReference() + ")";
            corEmailService.sendEmailToCaseworker(subject, decisionIssuedMessage);
            corEmailService.sendEmailToDwp(subject, decisionIssuedMessage);
        } else {
            String juiUrl = juiUrlGenerator.generateUrl(caseDetails);
            String decisionIssuedMessage = emailMessageBuilder.getDecisionRejectedMessage(caseDetails, tribunalViewResponse.getReason(), juiUrl);
            String subject = "Tribunal view rejected (" + caseDetails.getData().getCaseReference() + ")";
            corEmailService.sendEmailToCaseworker(subject, decisionIssuedMessage);
        }
    }
}
