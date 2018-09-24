package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohDecision {

    private final String onlineHearingId;
    private final String decisionAward;
    private final String decisionHeader;
    private final String decisionReason;
    private final String decisionText;

    public CohDecision(@JsonProperty(value = "online_hearing_id") String onlineHearingId,
                       @JsonProperty(value = "decision_award") String decisionAward,
                       @JsonProperty(value = "decision_header") String decisionHeader,
                       @JsonProperty(value = "decision_reason") String decisionReason,
                       @JsonProperty(value = "decision_text") String decisionText) {
        this.onlineHearingId = onlineHearingId;
        this.decisionAward = decisionAward;
        this.decisionHeader = decisionHeader;
        this.decisionReason = decisionReason;
        this.decisionText = decisionText;
    }

    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    public String getDecisionAward() {
        return decisionAward;
    }

    public String getDecisionHeader() {
        return decisionHeader;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public String getDecisionText() {
        return decisionText;
    }

}
