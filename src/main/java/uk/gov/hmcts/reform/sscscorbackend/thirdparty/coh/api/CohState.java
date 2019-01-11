package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohState {
    private final String stateName;
    private final String stateDateTime;

    public CohState(@JsonProperty(value = "state_name") String stateName, @JsonProperty(value = "state_datetime") String stateDateTime) {
        this.stateName = stateName;
        this.stateDateTime = stateDateTime;
    }

    public String getStateName() {
        return stateName;
    }

    public String getStateDateTime() {
        return stateDateTime;
    }
}
