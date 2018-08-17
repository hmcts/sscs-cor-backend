package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohQuestionReference {
    private String questionId;
    private String questionHeaderText;
    private int questionOrdinal;

    public CohQuestionReference(
            @JsonProperty(value = "question_id") String questionId,
            @JsonProperty(value = "question_ordinal") int questionOrdinal,
            @JsonProperty(value = "question_header_text") String questionHeaderText
    ) {
        this.questionId = questionId;
        this.questionOrdinal = questionOrdinal;
        this.questionHeaderText = questionHeaderText;
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
}
