package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.client.methods.RequestBuilder.get;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class GetListOfQuestions extends BaseFunctionTest {
    @Test
    public void getAListOfQuestionTitles() throws IOException {
        String hearingId = createHearing();
        String questionId = createQuestion(hearingId);

        JSONObject questionRound = getQuestions(hearingId);
        JSONArray questions = questionRound.getJSONArray("questions");

        assertThat(questions.length(), is(1));
        assertThat(questions.getJSONObject(0).getString("question_header_text"), is("question header"));
        assertThat(questions.getJSONObject(0).getString("question_id"), is(questionId));
    }

    private JSONObject getQuestions(String hearingId) throws IOException {
        HttpResponse getQuestionResponse = client.execute(get(baseUrl + "/continuous-online-hearings/" + hearingId)
                .build());

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getQuestionResponse.getEntity());

        return new JSONObject(responseBody);
    }
}
