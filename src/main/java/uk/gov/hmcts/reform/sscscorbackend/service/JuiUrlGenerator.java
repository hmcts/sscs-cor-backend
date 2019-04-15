package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

@Component
public class JuiUrlGenerator {
    private final String juiBaseUrl;
    private static final String JUI_CASE_PATH = "/case/SSCS/Benefit/{CASE_ID}/summary";

    public JuiUrlGenerator(@Value("${juiBaseUrl}") String juiBaseUrl) {
        this.juiBaseUrl = juiBaseUrl;
    }

    public String generateUrl(SscsCaseDetails sscsCaseDetails) {
        return juiBaseUrl + JUI_CASE_PATH.replace("{CASE_ID}", "" + sscsCaseDetails.getId());
    }
}
