package uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseData {
    private String onlineHearingId;

    private Panel onlinePanel;

    @JsonCreator
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
