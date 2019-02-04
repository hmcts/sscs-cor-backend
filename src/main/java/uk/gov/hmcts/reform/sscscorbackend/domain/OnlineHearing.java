package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnlineHearing {
    private String onlineHearingId;
    private String appellantName;
    private String caseReference;
    private Decision decision;
    private FinalDecision finalDecision;

    public OnlineHearing(String onlineHearingId, String appellantName, String caseReference, Decision decision, FinalDecision finalDecision) {
        this.onlineHearingId = onlineHearingId;
        this.appellantName = appellantName;
        this.caseReference = caseReference;
        this.decision = decision;
        this.finalDecision = finalDecision;
    }

    @ApiModelProperty(example = "ID_1", required = true)
    @JsonProperty(value = "online_hearing_id")
    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    @ApiModelProperty(example = "Joe Smith", required = true)
    @JsonProperty(value = "appellant_name")
    public String getAppellantName() {
        return appellantName;
    }

    @ApiModelProperty(example = "SC112/233", required = true)
    @JsonProperty(value = "case_reference")
    public String getCaseReference() {
        return caseReference;
    }

    @JsonProperty(value = "decision")
    public Decision getDecision() {
        return decision;
    }

    @JsonProperty(value = "final_decision")
    public FinalDecision getFinalDecision() {
        return finalDecision;
    }
}
