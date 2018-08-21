package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohState {
    private final String stateName;

    public CohState(@JsonProperty(value = "state_name") String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }
}
