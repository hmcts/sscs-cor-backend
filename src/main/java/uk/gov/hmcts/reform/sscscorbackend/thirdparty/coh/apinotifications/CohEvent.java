package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CohEvent {
    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("online_hearing_id")
    private String onlineHearingId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("expiry_date")
    private String expiryDate;

    @JsonProperty("reason")
    private String reason;

    public CohEvent() {
        // for Json
    }

    public CohEvent(String caseId, String onlineHearingId, String eventType, String expiryDate, String reason) {
        this.caseId = caseId;
        this.onlineHearingId = onlineHearingId;
        this.eventType = eventType;
        this.expiryDate = expiryDate;
        this.reason = reason;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getReason() {
        return reason;
    }
}

