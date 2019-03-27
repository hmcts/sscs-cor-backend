package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.Test;

public class GetAnOnlineHearingTest extends BaseFunctionTest {

    @Test
    public void getAnOnlineHearing() throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearingWithQuestion(true);

        JSONObject onlineHearingResponse = sscsCorBackendRequests.getOnlineHearing(onlineHearing.getEmailAddress());
        String onlineHearingId = onlineHearingResponse.getString("online_hearing_id");
        JSONObject decision = onlineHearingResponse.optJSONObject("decision");

        assertThat(onlineHearingId, is(onlineHearing.getHearingId()));
        assertThat(decision, is(nullValue()));
    }

    @Test
    public void getAnOnlineHearingWithDecision() throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearingWithQuestion(true);
        answerQuestion(onlineHearing.getHearingId(), onlineHearing.getQuestionId());
        createAndIssueDecision(onlineHearing.getHearingId(), onlineHearing.getCaseId());

        JSONObject onlineHearingResponse = sscsCorBackendRequests.getOnlineHearing(onlineHearing.getEmailAddress());
        String onlineHearingId = onlineHearingResponse.getString("online_hearing_id");
        JSONObject decision = onlineHearingResponse.getJSONObject("decision");

        assertThat(onlineHearingId, is(onlineHearing.getHearingId()));
        assertThat(decision.getString("decision_state"), is("decision_issued"));
        assertThat(decision.getString("decision_state_datetime"), is(notNullValue()));
        assertThat(decision.has("appellant_reply"), is(false));
        assertThat(decision.has("appellant_reply_datetime"), is(false));
    }

    @Test
    public void getAnOnlineHearingWithDecisionAndAppellantReplyAccepted() throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearingWithQuestion(true);
        answerQuestion(onlineHearing.getHearingId(), onlineHearing.getQuestionId());
        createAndIssueDecision(onlineHearing.getHearingId(), onlineHearing.getCaseId());
        recordAppellantViewResponse(onlineHearing.getHearingId(), "decision_accepted", "decision_accepted");

        JSONObject onlineHearingResponse = sscsCorBackendRequests.getOnlineHearing(onlineHearing.getEmailAddress());
        String onlineHearingId = onlineHearingResponse.getString("online_hearing_id");
        JSONObject decision = onlineHearingResponse.getJSONObject("decision");

        assertThat(onlineHearingId, is(onlineHearing.getHearingId()));

        assertThat(decision.getString("decision_state"), is("decision_issued"));
        assertThat(decision.getString("decision_state_datetime"), is(notNullValue()));
        assertThat(decision.getString("appellant_reply"), is("decision_accepted"));
        assertThat(decision.getString("appellant_reply_datetime"), is(notNullValue()));
    }
}
