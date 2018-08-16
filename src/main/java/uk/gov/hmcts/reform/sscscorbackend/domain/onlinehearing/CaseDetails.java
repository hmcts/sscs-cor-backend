package uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseDetails {
    @JsonProperty(value = "id")
    String caseId;

    @JsonProperty(value = "case_data")
    CaseData caseData;

    public CaseDetails(@JsonProperty(value = "id") String caseId,
                       @JsonProperty(value = "case_data") CaseData caseData) {
        this.caseId = caseId;
        this.caseData = caseData;
    }

    public String getCaseId() {
        return caseId;
    }

    public CaseData getCaseData() {
        return caseData;
    }
}