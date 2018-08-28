package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohOnlineHearing {
    private String onlineHearingId;

    public CohOnlineHearing(@JsonProperty(value = "online_hearing_id") String onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public String getOnlineHearingId() {
        return onlineHearingId;
    }
}
