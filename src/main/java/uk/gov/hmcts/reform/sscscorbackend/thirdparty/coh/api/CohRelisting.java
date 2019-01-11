package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohRelisting {
    private final String reason;

    public CohRelisting(@JsonProperty(value = "reason")String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
