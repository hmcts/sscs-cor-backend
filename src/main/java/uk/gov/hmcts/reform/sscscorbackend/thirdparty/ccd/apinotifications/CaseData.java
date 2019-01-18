package uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseData {
    private String onlineHearingId;
    private String assignedToJudge;
    private String assignedToDisabilityMember;
    private String assignedToMedicalMember;

    @JsonCreator
    public CaseData(
            @JsonProperty(value = "onlineHearingId") String onlineHearingId,
            @JsonProperty(value = "assignedToJudge")String assignedToJudge,
            @JsonProperty(value = "assignedToDisabilityMember")String assignedToDisabilityMember,
            @JsonProperty(value = "assignedToMedicalMember")String assignedToMedicalMember) {
        this.onlineHearingId = onlineHearingId;
        this.assignedToJudge = assignedToJudge;
        this.assignedToDisabilityMember = assignedToDisabilityMember;
        this.assignedToMedicalMember = assignedToMedicalMember;
    }

    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    public String getAssignedToJudge() {
        return assignedToJudge;
    }

    public String getAssignedToDisabilityMember() {
        return assignedToDisabilityMember;
    }

    public String getAssignedToMedicalMember() {
        return assignedToMedicalMember;
    }
}
