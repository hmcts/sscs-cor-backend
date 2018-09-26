package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Decision {

    private final String onlineHearingId;
    private final String decisionAward;
    private final String decisionHeader;
    private final String decisionReason;
    private final String decisionText;
    private final String decisionState;

    public Decision(String onlineHearingId,
                    String decisionAward,
                    String decisionHeader,
                    String decisionReason,
                    String decisionText,
                    String decisionState) {
        this.onlineHearingId = onlineHearingId;
        this.decisionAward = decisionAward;
        this.decisionHeader = decisionHeader;
        this.decisionReason = decisionReason;
        this.decisionText = decisionText;
        this.decisionState = decisionState;
    }

    @ApiModelProperty(example = "FINAL", required = true)
    @JsonProperty(value = "decision_award")
    public String getDecisionAward() {
        return decisionAward;
    }

    @ApiModelProperty(example = "Decision header", required = true)
    @JsonProperty(value = "decision_header")
    public String getDecisionHeader() {
        return decisionHeader;
    }

    @ApiModelProperty(example = "Decision reason", required = true)
    @JsonProperty(value = "decision_reason")
    public String getDecisionReason() {
        return decisionReason;
    }

    @ApiModelProperty(example = "Decision text", required = true)
    @JsonProperty(value = "decision_text")
    public String getDecisionText() {
        return decisionText;
    }

    @ApiModelProperty(example = "decision_issued", required = true)
    @JsonProperty(value = "decision_state")
    public String getDecisionState() {
        return decisionState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Decision decision = (Decision) o;
        return Objects.equals(onlineHearingId, decision.onlineHearingId) &&
                Objects.equals(decisionAward, decision.decisionAward) &&
                Objects.equals(decisionHeader, decision.decisionHeader) &&
                Objects.equals(decisionReason, decision.decisionReason) &&
                Objects.equals(decisionText, decision.decisionText) &&
                Objects.equals(decisionState, decision.decisionState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(onlineHearingId, decisionAward, decisionHeader, decisionReason, decisionText, decisionState);
    }

    @Override
    public String toString() {
        return "Decision{" +
                "  onlineHearingId='" + onlineHearingId + '\'' +
                ", decisionAward='" + decisionAward + '\'' +
                ", decisionHeader='" + decisionHeader + '\'' +
                ", decisionReason='" + decisionReason + '\'' +
                ", decisionText='" + decisionText + '\'' +
                ", decisionState='" + decisionState + '\'' +
                '}';
    }
}
