package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import static uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.HistoryEventExtractor.getStateDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

public class CohAnswer {
    private final String answerId;
    private final String answerText;
    private final CohState currentAnswerState;
    private final List<CohState> history;

    public CohAnswer(@JsonProperty(value = "answer_id") String answerId,
                     @JsonProperty(value = "answer_text") String answerText,
                     @JsonProperty(value = "current_answer_state") CohState currentAnswerState,
                     @JsonProperty(value = "history") List<CohState> history) {
        this.answerId = answerId;
        this.answerText = answerText;
        this.currentAnswerState = currentAnswerState;
        this.history = history;
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

    public List<CohState> getHistory() {
        return history;
    }

    public Optional<String> getAnsweredDate() {
        return getStateDate(history, "answer_submitted");
    }
}
