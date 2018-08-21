package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.equalTo;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.draft;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.submitted;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionSummary;

public class ListQuestionsTest extends BaseIntegrationTest {
    @Test
    public void getQuestion() {
        String hearingId = "1";
        QuestionSummary firstQuestionSummary = new QuestionSummary("first-id", "first question", draft);
        QuestionSummary secondQuestionSummary = new QuestionSummary("second-id", "second question", submitted);
        cohStub.stubGetAllQuestionRounds(hearingId, firstQuestionSummary, secondQuestionSummary);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings/" + hearingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("questions[0].question_id", equalTo(firstQuestionSummary.getId()))
                .body("questions[0].question_header_text", equalTo(firstQuestionSummary.getQuestionHeaderText()))
                .body("questions[0].answer_state", equalTo("draft"))
                .body("questions[1].question_id", equalTo(secondQuestionSummary.getId()))
                .body("questions[1].question_header_text", equalTo(secondQuestionSummary.getQuestionHeaderText()))
                .body("questions[1].answer_state", equalTo("submitted"));
    }
}
