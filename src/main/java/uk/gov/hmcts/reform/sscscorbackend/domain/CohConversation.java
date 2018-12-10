package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CohConversation {
    private List<CohQuestion> questions;

    public CohConversation(@JsonProperty(value = "questions")List<CohQuestion> cohQuestions) {
        this.questions = cohQuestions;
    }

    public List<CohQuestion> getQuestions() {
        return questions;
    }
}
