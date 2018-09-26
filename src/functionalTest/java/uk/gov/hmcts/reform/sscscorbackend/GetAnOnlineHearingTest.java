package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.Test;

public class GetAnOnlineHearingTest extends BaseFunctionTest {
    private final String DECISION_AWARD = "FINAL";
    private final String DECISION_HEADER = "The decision";
    private final String DECISION_REASON = "Decision reason";
    private final String DECISION_TEXT = "The decision text";

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
        String decisionId = cohRequests.createDecision(expectedOnlineHearingId, DECISION_AWARD,
                DECISION_HEADER, DECISION_REASON, DECISION_TEXT);
        cohRequests.issueDecision(expectedOnlineHearingId, DECISION_AWARD,
                DECISION_HEADER, DECISION_REASON, DECISION_TEXT);

        JSONObject onlineHearing = sscsCorBackendRequests.getOnlineHearing(emailAddress);
        String onlineHearingId = onlineHearing.getString("online_hearing_id");
        String decisionAward = onlineHearing.getJSONObject("decision").getString("decision_award");

        assertThat(onlineHearingId, is(expectedOnlineHearingId));
        assertThat(onlineHearing.getJSONObject("decision").getString("decision_award"), is(DECISION_AWARD));
        assertThat(onlineHearing.getJSONObject("decision").getString("decision_header"), is(DECISION_HEADER));
        assertThat(onlineHearing.getJSONObject("decision").getString("decision_reason"), is(DECISION_REASON));
        assertThat(onlineHearing.getJSONObject("decision").getString("decision_text"), is(DECISION_TEXT));
    }
}
