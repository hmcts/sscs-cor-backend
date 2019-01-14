package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CohQuestionRound {
    private List<CohQuestionReference> questionReferences;
    private int deadlineExtensionCount;
    private CohState questionRoundState;

    public CohQuestionRound(@JsonProperty(value = "question_references") List<CohQuestionReference> questionReferences,
                            @JsonProperty(value = "deadline_extension_count") int deadlineExtensionCount,
                            @JsonProperty(value = "question_round_state")  CohState questionRoundState) {
        this.questionReferences = questionReferences;
        this.deadlineExtensionCount = deadlineExtensionCount;
        this.questionRoundState = questionRoundState;
    }

    public List<CohQuestionReference> getQuestionReferences() {
        return questionReferences;
    }

    public int getDeadlineExtensionCount() {
        return deadlineExtensionCount;
    }

    public CohState getQuestionRoundState() {
        return questionRoundState;
    }
}
