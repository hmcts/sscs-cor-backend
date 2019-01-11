package uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CcdEvent {
    @JsonProperty(value = "case_details")
    private CaseDetails caseDetails;

    public CcdEvent(@JsonProperty(value = "case_details") CaseDetails caseDetails) {
        this.caseDetails = caseDetails;
    }

    public CaseDetails getCaseDetails() {
        return caseDetails;
    }
}
