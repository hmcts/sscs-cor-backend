package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CohQuestionReference {
    private String questionId;
    private String questionHeaderText;
    private int questionOrdinal;
    private List<CohAnswer> answers;

    public CohQuestionReference(
            @JsonProperty(value = "question_id") String questionId,
            @JsonProperty(value = "question_ordinal") int questionOrdinal,
            @JsonProperty(value = "question_header_text") String questionHeaderText,
            @JsonProperty(value = "answers") List<CohAnswer> answers
    ) {
        this.questionId = questionId;
        this.questionOrdinal = questionOrdinal;
        this.questionHeaderText = questionHeaderText;
        this.answers = answers;
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

    public List<CohAnswer> getAnswers() {
        return answers;
    }
}
