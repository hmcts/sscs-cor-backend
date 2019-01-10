package uk.gov.hmcts.reform.sscscorbackend.domain;

public class CohRelisting {
    private final String reason;

    public CohRelisting(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
