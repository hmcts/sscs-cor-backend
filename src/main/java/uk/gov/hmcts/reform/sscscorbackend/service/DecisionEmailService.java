package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.TribunalViewResponse;

@Service
public class DecisionEmailService {
    private final CorEmailService corEmailService;
    private final DwpEmailMessageBuilder dwpEmailMessageBuilder;

    public DecisionEmailService(
            CorEmailService corEmailService,
            DwpEmailMessageBuilder dwpEmailMessageBuilder
    ) {
        this.corEmailService = corEmailService;
        this.dwpEmailMessageBuilder = dwpEmailMessageBuilder;
    }

    public void sendEmail(SscsCaseDetails caseDetails, TribunalViewResponse tribunalViewResponse) {
        if (tribunalViewResponse.getReply().equals("decision_accepted")) {
            String decisionIssuedMessage = dwpEmailMessageBuilder.getDecisionAcceptedMessage(caseDetails);
            String subject = "Tribunal view accepted (" + caseDetails.getData().getCaseReference() + ")";
            corEmailService.sendEmailToCaseworker(subject, decisionIssuedMessage);
            corEmailService.sendEmailToDwp(subject, decisionIssuedMessage);
        } else {
            String decisionIssuedMessage = dwpEmailMessageBuilder.getDecisionRejectedMessage(caseDetails, tribunalViewResponse.getReason());
            String subject = "Tribunal view rejected (" + caseDetails.getData().getCaseReference() + ")";
            corEmailService.sendEmailToCaseworker(subject, decisionIssuedMessage);
        }
    }
}
