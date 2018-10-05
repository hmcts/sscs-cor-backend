package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.notNullValue;
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
    
    private String createRandomEmail() {
        int randomNumber = (int) (Math.random() * 10000000);
        String emailAddress = "test" + randomNumber + "@hmcts.net";
        System.out.println("emailAddress " + emailAddress);
        return emailAddress;
    }

    @Test
    public void getAnOnlineHearing() throws IOException {
        String emailAddress = createRandomEmail();
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
        String emailAddress = createRandomEmail();
        String caseId = sscsCorBackendRequests.createCase(emailAddress);
        String expectedOnlineHearingId = cohRequests.createHearing(caseId);
        String questionId = cohRequests.createQuestion(expectedOnlineHearingId);
        cohRequests.issueQuestionRound(expectedOnlineHearingId);
        cohRequests.createAnswer(expectedOnlineHearingId, questionId, "Valid answer");
        cohRequests.createDecision(expectedOnlineHearingId, decisionAward,
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
        assertThat(decision.getString("decision_state"), is("decision_issued"));
        assertThat(decision.getString("decision_state_datetime"), is(notNullValue()));
    }
}
