package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.Test;

public class GetAnOnlineHearingTest extends BaseFunctionTest {
    private final String decisionAward = "FINAL";
    private final String decisionHeader = "The decision";
    private final String decisionReason = "Decision reason";
    private final String decisionText = "The decision text";

    @Test
    public void getAnOnlineHearing() throws IOException {
        int randomNumber = (int) (Math.random() * 10000000);
        String emailAddress = "test" + String.valueOf(randomNumber) + "@hmcts.net";
        System.out.println("emailAddress " + emailAddress);
        String caseId = sscsCorBackendRequests.createCase(emailAddress);
        String expectedOnlineHearingId = cohRequests.createHearing(caseId);

        JSONObject onlineHearing = sscsCorBackendRequests.getOnlineHearing(emailAddress);
        String onlineHearingId = onlineHearing.getString("online_hearing_id");
        JSONObject decision = onlineHearing.optJSONObject("decision");

        assertThat(onlineHearingId, is(expectedOnlineHearingId));
        assertThat(decision, is(nullValue()));
    }

    @Test
    public void getAnOnlineHearingWithDecision() throws IOException, InterruptedException {
        int randomNumber = (int) (Math.random() * 10000000);
        String emailAddress = "test" + String.valueOf(randomNumber) + "@hmcts.net";
        System.out.println("emailAddress " + emailAddress);
        String caseId = sscsCorBackendRequests.createCase(emailAddress);
        String expectedOnlineHearingId = cohRequests.createHearing(caseId);
        String questionId = cohRequests.createQuestion(expectedOnlineHearingId);
        cohRequests.issueQuestionRound(expectedOnlineHearingId);
        String answerId = cohRequests.createAnswer(expectedOnlineHearingId, questionId, "Valid answer");
        String decisionId = cohRequests.createDecision(expectedOnlineHearingId, decisionAward,
                decisionHeader, decisionReason, decisionText);
        cohRequests.issueDecision(expectedOnlineHearingId, decisionAward,
                decisionHeader, decisionReason, decisionText);

        JSONObject onlineHearing = sscsCorBackendRequests.getOnlineHearing(emailAddress);
        String onlineHearingId = onlineHearing.getString("online_hearing_id");
        JSONObject decision = onlineHearing.getJSONObject("decision");

        assertThat(onlineHearingId, is(expectedOnlineHearingId));
        assertThat(decision.getString("decision_award"), is(decisionAward));
        assertThat(decision.getString("decision_header"), is(decisionHeader));
        assertThat(decision.getString("decision_reason"), is(decisionReason));
        assertThat(decision.getString("decision_text"), is(decisionText));
        // TODO: change this to check for "decision_issued" once it's possible to issue decisions
        assertThat(decision.getString("decision_state"), is("decision_issue_pending"));
    }
}
