package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CohQuestionReference {
    private String questionId;
    private String questionHeaderText;
    private String questionBodyText;
    private int questionOrdinal;
    private String deadlineExpiryDate;
    private List<CohAnswer> answers;

    public CohQuestionReference(
            @JsonProperty(value = "question_id") String questionId,
            @JsonProperty(value = "question_ordinal") int questionOrdinal,
            @JsonProperty(value = "question_header_text") String questionHeaderText,
            @JsonProperty(value = "question_body_text") String questionBodyText,
            @JsonProperty(value = "deadline_expiry_date") String deadlineExpiryDate,
            @JsonProperty(value = "answers") List<CohAnswer> answers
    ) {
        this.questionId = questionId;
        this.questionOrdinal = questionOrdinal;
        this.questionHeaderText = questionHeaderText;
        this.questionBodyText = questionBodyText;
        this.deadlineExpiryDate = deadlineExpiryDate;
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

    public String getQuestionBodyText() {
        return questionBodyText;
    }

    public String getDeadlineExpiryDate() {
        return deadlineExpiryDate;
    }

    public List<CohAnswer> getAnswers() {
        return answers;
    }
}
