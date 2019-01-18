package uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseDetails {
    @JsonProperty(value = "id")
    private String caseId;

    @JsonProperty(value = "case_data")
    private CaseData caseData;
    @JsonProperty(value = "case_data_before")
    private CaseData caseDataBefore;

    public CaseDetails(@JsonProperty(value = "id") String caseId,
                       @JsonProperty(value = "case_data") CaseData caseData,
                       @JsonProperty(value = "case_data_before") CaseData caseDataBefore
    ) {
        this.caseId = caseId;
        this.caseData = caseData;
        this.caseDataBefore = caseDataBefore;
    }

    public String getCaseId() {
        return caseId;
    }

    public CaseData getCaseData() {
        return caseData;
    }

    public CaseData getCaseDataBefore() {
        return caseDataBefore;
    }
}
