package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class GetListOfQuestionsTest extends BaseFunctionTest {
    @Test
    public void getAListOfQuestionTitles() throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearingWithQuestion(false);

        JSONObject questionRound = sscsCorBackendRequests.getQuestions(onlineHearing.getHearingId());
        JSONArray questions = questionRound.getJSONArray("questions");

        assertThat(questions.length(), is(1));
        assertThat(questions.getJSONObject(0).getString("question_header_text"), is("question header"));
        assertThat(questions.getJSONObject(0).getString("question_id"), is(onlineHearing.getQuestionId()));
    }
}
