package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CohOnlineHearings {
    private List<CohOnlineHearing> onlineHearings;

    public CohOnlineHearings(@JsonProperty(value = "online_hearings")List<CohOnlineHearing> onlineHearings) {
        this.onlineHearings = onlineHearings;
    }

    public List<CohOnlineHearing> getOnlineHearings() {
        return onlineHearings;
    }
}
