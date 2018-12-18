package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

public class Activity {
    private final String activity;
    private final int points;

    public Activity(String activity, int points) {
        this.activity = activity;
        this.points = points;
    }

    @ApiModelProperty(example = "an activity", required = true)
    @JsonProperty(value = "activity")
    public String getActivity() {
        return activity;
    }

    @ApiModelProperty(example = "5", required = true)
    @JsonProperty(value = "points")
    public int getPoints() {
        return points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Activity activity1 = (Activity) o;
        return points == activity1.points &&
                Objects.equals(activity, activity1.activity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activity, points);
    }
}
