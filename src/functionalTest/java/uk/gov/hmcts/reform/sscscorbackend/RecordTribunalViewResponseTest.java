package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.Test;

public class RecordTribunalViewResponseTest extends BaseFunctionTest {

    @Test
    public void recordAcceptedResponse() throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearingWithQuestion(true);
        answerQuestion(onlineHearing.getHearingId(), onlineHearing.getQuestionId());
        createAndIssueDecision(onlineHearing.getHearingId(), onlineHearing.getCaseId());

        String reply = "decision_accepted";
        String reason = "";

        sscsCorBackendRequests.recordTribunalViewResponse(onlineHearing.getHearingId(), reply, reason);
        JSONObject decisionRepliesJson = cohRequests.getDecisionReplies(onlineHearing.getHearingId());
        int decisionReplyCount = decisionRepliesJson.getJSONArray("decision_replies").length();
        JSONObject firstDecisionReply = decisionRepliesJson.getJSONArray("decision_replies").getJSONObject(0);

        assertThat(decisionReplyCount, is(1));
        assertThat(firstDecisionReply.getString("decision_reply"), is(reply));
        // COH won't accept an empty reason, therefore it is set to the reply if it's empty
        String cohExpectedReason = "decision_accepted";
        assertThat(firstDecisionReply.getString("decision_reply_reason"), is(cohExpectedReason));
    }

    @Test
    public void recordRejectedResponse() throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearingWithQuestion(true);
        answerQuestion(onlineHearing.getHearingId(), onlineHearing.getQuestionId());
        createAndIssueDecision(onlineHearing.getHearingId(), onlineHearing.getCaseId());

        String reply = "decision_rejected";
        String reason = "I reject this view because...";

        sscsCorBackendRequests.recordTribunalViewResponse(onlineHearing.getHearingId(), reply, reason);
        JSONObject decisionRepliesJson = cohRequests.getDecisionReplies(onlineHearing.getHearingId());
        int decisionReplyCount = decisionRepliesJson.getJSONArray("decision_replies").length();
        JSONObject firstDecisionReply = decisionRepliesJson.getJSONArray("decision_replies").getJSONObject(0);

        assertThat(decisionReplyCount, is(1));
        assertThat(firstDecisionReply.getString("decision_reply"), is(reply));
        assertThat(firstDecisionReply.getString("decision_reply_reason"), is(reason));
    }
}
