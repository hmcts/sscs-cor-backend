package uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOnlineHearingRequest {

    @JsonCreator
    public CreateOnlineHearingRequest(
            @JsonProperty(value = "case_id")String caseId,
            @JsonProperty(value = "panel") List<PanelRequest> panel) {
        this.caseId = caseId;
        this.panel = panel;
        this.startDate = LocalDateTime.now();
    }

    private String caseId;

    @JsonProperty(value = "jurisdiction")
    private String jurisdiction = "SSCS";

    private List<PanelRequest> panel;

    @JsonProperty(value = "start_date")
    private LocalDateTime startDate;

    @JsonProperty(value = "state")
    private String state = "continuous_online_hearing_started";

    public String getCaseId() {
        return caseId;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public List<PanelRequest> getPanel() {
        return panel;
    }

    public LocalDateTime getStartDate() {
        return startDate;
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
