package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.submitted;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class GetAQuestion extends BaseFunctionTest {

    @Test
    public void getsAndAnswersAQuestion() throws IOException, InterruptedException {
        String hearingId = cohRequests.createHearing();
        String questionId = cohRequests.createQuestion(hearingId);
        cohRequests.issueQuestionRound(hearingId);

        JSONObject jsonObject = sscsCorBackendRequests.getQuestion(hearingId, questionId);
        String questionBodyText = jsonObject.getString("question_body_text");
        String answer = jsonObject.optString("answer", null);

        assertThat(questionBodyText, is("question text"));
        assertThat(answer, is(nullValue()));

        String expectedAnswer = "an answer";
        sscsCorBackendRequests.answerQuestion(hearingId, questionId, expectedAnswer);

        jsonObject = sscsCorBackendRequests.getQuestion(hearingId, questionId);
        answer = jsonObject.optString("answer", expectedAnswer);

        assertThat(answer, is(expectedAnswer));

        sscsCorBackendRequests.submitAnswer(hearingId, questionId);

        JSONObject questionRound = sscsCorBackendRequests.getQuestions(hearingId);
        JSONArray questions = questionRound.getJSONArray("questions");
        assertThat(questions.getJSONObject(0).getString("answer_state"), is(submitted.name()));
    }
}