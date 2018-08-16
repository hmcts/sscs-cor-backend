package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CohQuestionRound {
    private List<CohQuestionReference> questionReferences;

    public CohQuestionRound(@JsonProperty(value = "question_references") List<CohQuestionReference> questionReferences) {
        this.questionReferences = questionReferences;
    }

    public List<CohQuestionReference> getQuestionReferences() {
        return questionReferences;
    }
}
