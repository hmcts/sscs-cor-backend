package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.client.methods.RequestBuilder.get;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class GetAQuestion extends BaseFunctionTest {

    @Test
    public void getsAndAnswersAQuestion() throws IOException, InterruptedException {
        String hearingId = createHearing();
        String questionId = createQuestion(hearingId);
        issueQuestionRound(hearingId);

        JSONObject jsonObject = getQuestion(hearingId, questionId);
        String questionBodyText = jsonObject.getString("question_body_text");
        String answer = jsonObject.optString("answer", null);

        assertThat(questionBodyText, is("question text"));
        assertThat(answer, is(nullValue()));

        Thread.sleep(60000L);

        String expectedAnswer = "an answer";
        answerQuestion(hearingId, questionId, expectedAnswer);

        jsonObject = getQuestion(hearingId, questionId);
        answer = jsonObject.optString("answer", expectedAnswer);

        assertThat(answer, is(expectedAnswer));
    }

    private JSONObject getQuestion(String hearingId, String questionId) throws IOException {
        HttpResponse getQuestionResponse = client.execute(get(baseUrl + "/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .build());

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getQuestionResponse.getEntity());

        return new JSONObject(responseBody);
    }
}