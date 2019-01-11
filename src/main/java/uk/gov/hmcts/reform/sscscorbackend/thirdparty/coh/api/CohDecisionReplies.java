package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CohDecisionReplies {

    private final List<CohDecisionReply> decisionReplies;

    public CohDecisionReplies(@JsonProperty(value = "decision_replies") List<CohDecisionReply> decisionReplies) {
        this.decisionReplies = decisionReplies;
    }

    public List<CohDecisionReply> getDecisionReplies() {
        return decisionReplies;
    }
}
