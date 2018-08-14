package uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Panel {
    /*
    assigned to is the judge
     */
    public Panel(@JsonProperty(value = "assignedTo") String assignedTo,
                 @JsonProperty(value = "medicalMember") String medicalMember,
                 @JsonProperty(value = "disabilityQualifiedMember") String disabilityQualifiedMember) {
        this.assignedTo = assignedTo;
        this.medicalMember = medicalMember;
        this.disabilityQualifiedMember = disabilityQualifiedMember;
    }

    @JsonProperty(value = "assignedTo")
    String assignedTo;

    @JsonProperty(value = "medicalMember")
    String medicalMember;

    @JsonProperty(value = "disabilityQualifiedMember")
    String disabilityQualifiedMember;

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getMedicalMember() {
        return medicalMember;
    }

    public String getDisabilityQualifiedMember() {
        return disabilityQualifiedMember;
    }
}
