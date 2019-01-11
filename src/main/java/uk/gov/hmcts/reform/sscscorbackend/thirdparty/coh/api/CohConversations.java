package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohConversations {
    private CohConversation conversation;

    public CohConversations(@JsonProperty(value = "online_hearing")CohConversation conversation) {
        this.conversation = conversation;
    }

    public CohConversation getConversation() {
        return conversation;
    }
}
