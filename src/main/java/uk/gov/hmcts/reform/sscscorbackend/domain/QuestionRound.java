package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionRound {
    private List<QuestionSummary> questions;
    private final String deadlineExpiryDate;

    public QuestionRound(List<QuestionSummary> questions, String deadlineExpiryDate) {
        this.questions = questions;
        this.deadlineExpiryDate = deadlineExpiryDate;
    }

    @JsonProperty(value = "questions")
    public List<QuestionSummary> getQuestions() {
        return questions;
    }

    @JsonProperty(value = "deadline_expiry_date")
    public String getDeadlineExpiryDate() {
        return deadlineExpiryDate;
    }
}
