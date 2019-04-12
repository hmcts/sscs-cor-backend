package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

@Service
public class EmailMessageBuilder {
    private static final String HEADER_TEMPLATE = "Appeal reference number: {caseReference}\n" +
            "Appellant name: {firstName} {lastName}\n" +
            "Appellant NINO: {nino}\n";

    private static final String TEMPLATE = HEADER_TEMPLATE +
            "\n" +
            "{message}\n" +
            "\n" +
            "PIP Benefit Appeals\n" +
            "HMCTS\n";

    private static final String TEMPLATE_WITH_HEADING = "{heading}\n" +
            "\n" +
            TEMPLATE;
    private static final String DEAR_DWP = "Dear DWP\n";

    public String getAnswerMessage(SscsCaseDetails caseDetails) {
        String message = DEAR_DWP +
                "\n" +
                "The appellant has submitted additional information in relation to the above appeal. " +
                "Please see attached.\n\n" +
                "Please respond to this email if you have any comment.";
        return buildMessage(caseDetails, message);
    }

    public String getRelistedMessage(SscsCaseDetails caseDetails) {
        String message = DEAR_DWP +
                "\n" +
                "The tribunal panel have decided that a hearing is required for the above appeal. A hearing will be booked and details will be sent.";
        return buildMessageWithHeading(caseDetails, message, "Hearing required");
    }

    public String getQuestionMessage(SscsCaseDetails caseDetails) {
        String message = DEAR_DWP +
                "\n" +
                "The tribunal have sent some questions to the appellant in the above appeal.\n" +
                "Please see the questions attached.";
        return buildMessage(caseDetails, message);
    }

    public String getDecisionIssuedMessage(SscsCaseDetails caseDetails) {
        String message = DEAR_DWP +
                "\n" +
                "The tribunal panel have reached a view on the above appeal.\n" +
                "\n" +
                "The view is attached to this email. Please read it and reply, stating whether you agree or " +
                "disagree with it. Please provide reasons if you disagree.";
        return buildMessageWithHeading(caseDetails, message, "Preliminary view offered");
    }

    public String getDecisionAcceptedMessage(SscsCaseDetails caseDetails) {
        return buildMessage(caseDetails, "The appellant has accepted the tribunal's view.");
    }

    public String getDecisionRejectedMessage(SscsCaseDetails caseDetails, String reason) {
        return buildMessage(caseDetails, "The appellant has rejected the tribunal's view.\n" +
                "\n" +
                "Reasons for requesting a hearing:\n\n" + reason);
    }

    public String getAppellantStatementMessage(SscsCaseDetails sscsCaseDetails) {
        return buildMessage(sscsCaseDetails, "The appellant has added a statement to their appeal.");
    }

    private String buildMessageWithHeading(SscsCaseDetails caseDetails, String message, String heading) {
        String templateWithHeading = TEMPLATE_WITH_HEADING.replace("{heading}", heading);

        return buildMessageWithTemplate(caseDetails, message, templateWithHeading);
    }

    private String buildMessage(SscsCaseDetails caseDetails, String message) {
        return buildMessageWithTemplate(caseDetails, message, TEMPLATE);
    }

    private String buildMessageWithTemplate(SscsCaseDetails caseDetails, String message, String template) {
        SscsCaseData data = caseDetails.getData();
        Appellant appellant = data.getAppeal().getAppellant();
        Name name = appellant.getName();
        return template.replace("{firstName}", name.getFirstName())
                .replace("{lastName}", name.getLastName())
                .replace("{caseReference}", data.getCaseReference())
                .replace("{nino}", appellant.getIdentity().getNino())
                .replace("{message}", message);
    }
}
