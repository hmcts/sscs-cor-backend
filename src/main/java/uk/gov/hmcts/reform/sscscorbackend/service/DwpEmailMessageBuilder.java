package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

@Service
public class DwpEmailMessageBuilder {
    private static final String HEADER_TEMPLATE = "Appeal reference number: {caseReference}\n" +
            "Appellant name: {firstName} {lastName}\n" +
            "Appellant NINO: {nino}\n";

    private static final String TEMPLATE = HEADER_TEMPLATE +
            "\n" +
            "Dear DWP\n" +
            "\n" +
            "{message}\n" +
            "\n" +
            "PIP Benefit Appeals\n" +
            "HMCTS\n";

    private static final String HEARING_REQUIRED_TEMPLATE = "Hearing required\n" +
            "\n" +
            TEMPLATE;

    public String getAnswerMessage(SscsCaseDetails caseDetails) {
        String message = "The appellant has submitted additional information in relation to the above appeal. " +
                "Please see attached.\n\n" +
                "Please respond to this email if you have any comment.";
        return buildMessage(caseDetails, message, TEMPLATE);
    }

    public String getRelistedMessage(SscsCaseDetails caseDetails) {
        String message = "The tribunal panel have decided that a hearing is required for the above appeal. A hearing will be booked and details will be sent.";
        return buildMessage(caseDetails, message, HEARING_REQUIRED_TEMPLATE);
    }

    public String getQuestionMessage(SscsCaseDetails caseDetails) {
        String message = "The tribunal have sent some questions to the appellant in the above appeal.\n" +
                "Please see the questions attached.";
        return buildMessage(caseDetails, message, TEMPLATE);
    }

    private String buildMessage(SscsCaseDetails caseDetails, String message, String template) {
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
