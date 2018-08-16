package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohQuestionReference {
    private String questionHeaderText;
    private int questionOrdinal;

    public CohQuestionReference(
            @JsonProperty(value = "question_ordinal") int questionOrdinal,
            @JsonProperty(value = "question_header_text") String questionHeaderText
    ) {
        this.questionOrdinal = questionOrdinal;
        this.questionHeaderText = questionHeaderText;
    }

    public int getQuestionOrdinal() {
        return questionOrdinal;
    }

    public String getQuestionHeaderText() {
        return questionHeaderText;
    }
}
