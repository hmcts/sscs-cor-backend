package uk.gov.hmcts.reform.sscscorbackend.domain;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CohConversation {
    private List<CohQuestion> questions;

    public CohConversation(@JsonProperty(value = "questions")List<CohQuestion> cohQuestions) {
        this.questions = cohQuestions;
    }

    public List<CohQuestion> getQuestions() {
        return questions;
    }

    public Map<Integer, String> getRoundIssueDates() {
        if (questions != null) {
            return questions.stream()
                    .filter(question -> question.getIssueDate().isPresent())
                    .collect(toMap(CohQuestion::getQuestionRound, question -> question.getIssueDate().get()));
        } else {
            return Collections.emptyMap();
        }
    }
}
