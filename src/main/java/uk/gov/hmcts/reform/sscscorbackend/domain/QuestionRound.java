package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionRound {
    private List<QuestionSummary> questions;

    public QuestionRound(List<QuestionSummary> questions) {
        this.questions = questions;
    }

    @JsonProperty(value = "questions")
    public List<QuestionSummary> getQuestions() {
        return questions;
    }
}
