package uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOnlineHearingRequest {
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @JsonCreator
    public CreateOnlineHearingRequest(String caseId) {
        this.caseId = caseId;
        this.startDateTime = LocalDateTime.now();
    }

    @JsonProperty(value = "case_id")
    private String caseId;

    @JsonProperty(value = "jurisdiction")
    private String jurisdiction = "SSCS";

    @JsonProperty(value = "start_date")
    private String startDate;

    private LocalDateTime startDateTime;

    @JsonProperty(value = "state")
    private String state = "continuous_online_hearing_started";

    public String getCaseId() {
        return caseId;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public String getStartDate() {
        return startDateTime.format(formatter);
    }

    public String getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateOnlineHearingRequest that = (CreateOnlineHearingRequest) o;
        return Objects.equals(caseId, that.caseId)
                && Objects.equals(jurisdiction, that.jurisdiction)
                && Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {

        return Objects.hash(caseId, jurisdiction, state);
    }
}
