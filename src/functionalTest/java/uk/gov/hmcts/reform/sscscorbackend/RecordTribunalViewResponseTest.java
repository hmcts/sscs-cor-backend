package uk.gov.hmcts.reform.sscscorbackend;

import org.json.JSONObject;
import org.junit.Test;
import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RecordTribunalViewResponseTest extends BaseFunctionTest {
    private final String decisionAward = "FINAL";
    private final String decisionHeader = "The decision";
    private final String decisionReason = "Decision reason";
    private final String decisionText = "The decision text";

    @Test
    public void recordAcceptedResponse() throws IOException, InterruptedException {
        String emailAddress = createRandomEmail();
        String caseId = sscsCorBackendRequests.createCase(emailAddress);
        String hearingId = cohRequests.createHearing(caseId);
        String questionId = cohRequests.createQuestion(hearingId);
        cohRequests.issueQuestionRound(hearingId);
        cohRequests.createAnswer(hearingId, questionId, "Valid answer");
        cohRequests.createDecision(hearingId, decisionAward, decisionHeader, decisionReason, decisionText);
        cohRequests.issueDecision(hearingId, decisionAward, decisionHeader, decisionReason, decisionText);

        String reply = "decision_accepted";
        String reason = "";
        String cohExpectedReason = "decision_accepted";

        sscsCorBackendRequests.recordTribunalViewResponse(hearingId, reply, reason);
        JSONObject decisionRepliesJson = cohRequests.getDecisionReplies(hearingId);
        int decisionReplyCount = decisionRepliesJson.getJSONArray("decision_replies").length();
        JSONObject firstDecisionReply = decisionRepliesJson.getJSONArray("decision_replies").getJSONObject(0);

        assertThat(decisionReplyCount, is(1));
        assertThat(firstDecisionReply.getString("decision_reply"), is(reply));
        // COH won't accept an empty reason, therefore it is set to the reply if it's empty
        assertThat(firstDecisionReply.getString("decision_reply_reason"), is(cohExpectedReason));
    }

    @Test
    public void recordRejectedResponse() throws IOException, InterruptedException {
        String emailAddress = createRandomEmail();
        String caseId = sscsCorBackendRequests.createCase(emailAddress);
        String hearingId = cohRequests.createHearing(caseId);
        String questionId = cohRequests.createQuestion(hearingId);
        cohRequests.issueQuestionRound(hearingId);
        cohRequests.createAnswer(hearingId, questionId, "Valid answer");
        cohRequests.createDecision(hearingId, decisionAward, decisionHeader, decisionReason, decisionText);
        cohRequests.issueDecision(hearingId, decisionAward, decisionHeader, decisionReason, decisionText);

        String reply = "decision_rejected";
        String reason = "I reject this view because...";

        sscsCorBackendRequests.recordTribunalViewResponse(hearingId, reply, reason);
        JSONObject decisionRepliesJson = cohRequests.getDecisionReplies(hearingId);
        int decisionReplyCount = decisionRepliesJson.getJSONArray("decision_replies").length();
        JSONObject firstDecisionReply = decisionRepliesJson.getJSONArray("decision_replies").getJSONObject(0);

        assertThat(decisionReplyCount, is(1));
        assertThat(firstDecisionReply.getString("decision_reply"), is(reply));
        assertThat(firstDecisionReply.getString("decision_reply_reason"), is(reason));
    }
}
