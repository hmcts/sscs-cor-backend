package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class CohUpdateAnswer {
    private final String answerState;
    private final String answerText;

    public CohUpdateAnswer(String answerState, String answerText) {
        this.answerState = answerState;
        this.answerText = answerText;
    }

    @JsonProperty(value = "answer_state")
    public String getAnswerState() {
        return answerState;
    }

    @JsonProperty(value = "answer_text")
    public String getAnswerText() {
        return answerText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CohUpdateAnswer that = (CohUpdateAnswer) o;
        return Objects.equals(answerState, that.answerState) &&
                Objects.equals(answerText, that.answerText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answerState, answerText);
    }

    @Override
    public String toString() {
        return "CohUpdateAnswer{" +
                "answerState='" + answerState + '\'' +
                ", answerText='" + answerText + '\'' +
                '}';
    }
}
