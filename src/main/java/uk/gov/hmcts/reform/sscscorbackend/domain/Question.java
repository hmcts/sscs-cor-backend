package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Question {

    private final String onlineHearingId;
    private final String questionId;
    private final String questionHeaderText;

    public Question(@JsonProperty(value = "online_hearing_id") String onlineHearingId,
                    @JsonProperty(value = "question_id")String questionId,
                    @JsonProperty(value = "question_header_text") String questionHeaderText) {
        this.onlineHearingId = onlineHearingId;
        this.questionId = questionId;
        this.questionHeaderText = questionHeaderText;
    }

    @JsonProperty(value = "online_hearing_id")
    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    @JsonProperty(value = "question_id")
    public String getQuestionId() {
        return questionId;
    }

    @JsonProperty(value = "question_header_text")
    public String getQuestionHeaderText() {
        return questionHeaderText;
    }
}
