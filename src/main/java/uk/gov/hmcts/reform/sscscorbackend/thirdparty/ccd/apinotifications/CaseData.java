package uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseData {
    private String onlineHearingId;

    @JsonCreator
    public CaseData(@JsonProperty(value = "onlineHearingId") String onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public String getOnlineHearingId() {
        return onlineHearingId;
    }
}
