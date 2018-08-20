package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohAnswer {
    private final String answerId;
    private final String answerText;
    private final CohState currentAnswerState;

    public CohAnswer(@JsonProperty(value = "answer_id") String answerId,
                     @JsonProperty(value = "answer_text") String answerText,
                     @JsonProperty(value = "current_answer_state") CohState currentAnswerState) {
        this.answerId = answerId;
        this.answerText = answerText;
        this.currentAnswerState = currentAnswerState;
    }

    public String getAnswerId() {
        return answerId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public CohState getCurrentAnswerState() {
        return currentAnswerState;
    }
}
