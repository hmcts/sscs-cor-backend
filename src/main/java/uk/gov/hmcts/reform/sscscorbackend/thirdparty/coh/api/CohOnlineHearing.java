package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohOnlineHearing {
    private String onlineHearingId;
    private String ccdCaseId;

    public CohOnlineHearing(
            @JsonProperty(value = "online_hearing_id") String onlineHearingId,
            @JsonProperty(value = "case_id") String ccdCaseId
    ) {
        this.onlineHearingId = onlineHearingId;
        this.ccdCaseId = ccdCaseId;
    }

    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    public Long getCcdCaseId() {
        return Long.valueOf(ccdCaseId);
    }
}
