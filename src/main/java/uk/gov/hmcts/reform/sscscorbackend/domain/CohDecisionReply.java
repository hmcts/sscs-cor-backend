package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class CohDecisionReply {
    private final String reply;
    private final String replyReason;

    public CohDecisionReply(String reply, String replyReason) {
        this.reply = reply;
        this.replyReason = replyReason;
    }

    @JsonProperty(value = "decision_reply")
    public String getReply() {
        return reply;
    }

    @JsonProperty(value = "decision_reply_reason")
    public String getReplyReason() {
        return replyReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CohDecisionReply that = (CohDecisionReply) o;
        return Objects.equals(reply, that.reply) &&
                Objects.equals(replyReason, that.replyReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reply, replyReason);
    }

    @Override
    public String toString() {
        return "CohDecisionReply{" +
                "  reply='" + reply + '\'' +
                ", replyReason='" + replyReason + '\'' +
                '}';
    }
}
