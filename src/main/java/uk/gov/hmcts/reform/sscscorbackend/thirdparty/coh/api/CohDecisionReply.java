package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class CohDecisionReply {
    private final String reply;
    private final String replyReason;
    private String replyDateTime;
    private String authorReference;

    public CohDecisionReply(String reply, String replyReason) {
        this.reply = reply;
        this.replyReason = replyReason;
    }

    public CohDecisionReply(@JsonProperty(value = "decision_reply") String reply,
                       @JsonProperty(value = "decision_reply_reason") String replyReason,
                       @JsonProperty(value = "decision_reply_date") String replyDateTime,
                       @JsonProperty(value = "author_reference") String authorReference) {
        this.reply = reply;
        this.replyReason = replyReason;
        this.replyDateTime = replyDateTime;
        this.authorReference = authorReference;
    }

    @JsonProperty(value = "decision_reply")
    public String getReply() {
        return reply;
    }

    @JsonProperty(value = "decision_reply_reason")
    public String getReplyReason() {
        return replyReason;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getReplyDateTime() {
        return replyDateTime;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getAuthorReference() {
        return authorReference;
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
                Objects.equals(replyReason, that.replyReason) &&
                Objects.equals(replyDateTime, that.replyDateTime) &&
                Objects.equals(authorReference, that.authorReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reply, replyReason, replyDateTime, authorReference);
    }

    @Override
    public String toString() {
        return "CohDecisionReply{" +
                "  reply='" + reply + '\'' +
                ", replyReason='" + replyReason + '\'' +
                ", replyDateTime='" + replyDateTime + '\'' +
                ", authorReference='" + authorReference + '\'' +
                '}';
    }
}
