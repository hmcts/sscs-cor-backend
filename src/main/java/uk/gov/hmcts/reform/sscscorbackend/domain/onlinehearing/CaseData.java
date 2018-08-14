package uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseData {
    @JsonProperty(value = "onlineHearingId")
    String onlineHearingId;

    @JsonProperty(value = "onlinePanel")
    Panel onlinePanel;

    public CaseData(@JsonProperty(value = "onlineHearingId") String onlineHearingId,
                    @JsonProperty(value = "onlinePanel") Panel onlinePanel) {
        this.onlineHearingId = onlineHearingId;
        this.onlinePanel = onlinePanel;
    }

    public Panel getOnlinePanel() {
        return onlinePanel;
    }

    public String getOnlineHearingId() {
        return onlineHearingId;
    }
}
