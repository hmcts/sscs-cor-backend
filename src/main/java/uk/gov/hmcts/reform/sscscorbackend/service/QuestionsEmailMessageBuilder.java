package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

@Service
public class QuestionsEmailMessageBuilder {
    private static final String TEMPLATE = "Appellant: {firstName} {lastName}\n\n" +
            "Appeal reference number: {caseReference}\n\n" +
            "National Insurance number: {nino}\n\n" +
            "The tribunal have sent some questions to the appellant in the above appeal.\n" +
            "Please see the questions attached.\n\n" +
            "PIP Benefit Appeals\n" +
            "Her Majesty's Courts and Tribunal Service\n";
    
    public String getMessage(SscsCaseDetails caseDetails) {
        SscsCaseData data = caseDetails.getData();
        Appellant appellant = data.getAppeal().getAppellant();
        Name name = appellant.getName();
        return TEMPLATE.replace("{firstName}", name.getFirstName())
                .replace("{lastName}", name.getLastName())
                .replace("{caseReference}", data.getCaseReference())
                .replace("{nino}", appellant.getIdentity().getNino());
    }
}
