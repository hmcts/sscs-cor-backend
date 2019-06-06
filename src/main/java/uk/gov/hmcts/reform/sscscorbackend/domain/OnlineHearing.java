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
    private Long caseId;
    private Decision decision;
    private FinalDecision finalDecision;
    private boolean hasFinalDecision;

    public OnlineHearing(String onlineHearingId, String appellantName, String caseReference, Long caseId, Decision decision, FinalDecision finalDecision, boolean hasFinalDecision) {
        this.onlineHearingId = onlineHearingId;
        this.appellantName = appellantName;
        this.caseReference = caseReference;
        this.caseId = caseId;
        this.decision = decision;
        this.finalDecision = finalDecision;
        this.hasFinalDecision = hasFinalDecision;
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

    @ApiModelProperty(example = "123456789", required = true)
    @JsonProperty(value = "case_id")
    public Long getCaseId() {
        return caseId;
    }

    @JsonProperty(value = "decision")
    public Decision getDecision() {
        return decision;
    }

    @JsonProperty(value = "final_decision")
    public FinalDecision getFinalDecision() {
        return finalDecision;
    }

    @JsonProperty(value = "has_final_decision")
    public boolean isHasFinalDecision() {
        return hasFinalDecision;
    }
}
