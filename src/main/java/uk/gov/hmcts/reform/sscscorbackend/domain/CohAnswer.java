package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohAnswer {
    private final String answerId;
    private final String answerText;

    public CohAnswer(@JsonProperty(value = "answer_id")String answerId, @JsonProperty(value = "answer_text")String answerText) {
        this.answerId = answerId;
        this.answerText = answerText;
    }

    public String getAnswerId() {
        return answerId;
    }

    public String getAnswerText() {
        return answerText;
    }
}
