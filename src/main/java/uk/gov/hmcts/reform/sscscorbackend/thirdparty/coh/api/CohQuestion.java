package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import static uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.HistoryEventExtractor.getStateDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

public class CohQuestion {

    private final String onlineHearingId;
    private final int questionRound;
    private final String questionId;
    private final int questionOrdinal;
    private final String questionHeaderText;
    private final String questionBodyText;
    private final List<CohState> history;
    private final List<CohAnswer> answers;

    public CohQuestion(@JsonProperty(value = "online_hearing_id") String onlineHearingId,
                       @JsonProperty(value = "question_round") int questionRound,
                       @JsonProperty(value = "question_id")String questionId,
                       @JsonProperty(value = "question_ordinal") int questionOrdinal,
                       @JsonProperty(value = "question_header_text") String questionHeaderText,
                       @JsonProperty(value = "question_body_text") String questionBodyText,
                       @JsonProperty(value = "history") List<CohState> history,
                       @JsonProperty(value = "answers") List<CohAnswer> answers) {
        this.onlineHearingId = onlineHearingId;
        this.questionRound = questionRound;
        this.questionId = questionId;
        this.questionOrdinal = questionOrdinal;
        this.questionHeaderText = questionHeaderText;
        this.questionBodyText = questionBodyText;
        this.history = history;
        this.answers = answers;
    }

    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    public int getQuestionRound() {
        return questionRound;
    }

    public String getQuestionId() {
        return questionId;
    }

    public int getQuestionOrdinal() {
        return questionOrdinal;
    }

    public String getQuestionHeaderText() {
        return questionHeaderText;
    }

    public String getQuestionBodyText() {
        return questionBodyText;
    }

    public List<CohState> getHistory() {
        return history;
    }

    public List<CohAnswer> getAnswers() {
        return answers;
    }

    public Optional<String> getIssueDate() {
        return getStateDate(history, "question_issued");
    }

    public Optional<CohAnswer> getAnswer() {
        if (answers != null) {
            return answers.stream()
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getSubmittedDate() {
        return getAnswer().flatMap(CohAnswer::getAnsweredDate);
    }
}
