package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CohQuestionRound {
    private List<CohQuestionReference> questionReferences;
    private int deadlineExtensionCount;

    public CohQuestionRound(@JsonProperty(value = "question_references") List<CohQuestionReference> questionReferences,
                            @JsonProperty(value = "deadline_extension_count") int deadlineExtensionCount) {
        this.questionReferences = questionReferences;
        this.deadlineExtensionCount = deadlineExtensionCount;
    }

    public List<CohQuestionReference> getQuestionReferences() {
        return questionReferences;
    }

    public int getDeadlineExtensionCount() {
        return deadlineExtensionCount;
    }
}
