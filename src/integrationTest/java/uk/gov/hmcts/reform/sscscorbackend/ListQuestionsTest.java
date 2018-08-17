package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.equalTo;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionSummary;

public class ListQuestionsTest extends BaseIntegrationTest {
    @Test
    public void getQuestion() {
        String hearingId = "1";
        QuestionSummary firstQuestionSummary = new QuestionSummary("first-id", "first question");
        QuestionSummary secondQuestionSummary = new QuestionSummary("second-id", "second question");
        cohStub.stubGetAllQuestionRounds(hearingId, firstQuestionSummary, secondQuestionSummary);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings/" + hearingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("questions[0].question_id", equalTo(firstQuestionSummary.getId()))
                .body("questions[0].question_header_text", equalTo(firstQuestionSummary.getQuestionHeaderText()))
                .body("questions[1].question_id", equalTo(secondQuestionSummary.getId()))
                .body("questions[1].question_header_text", equalTo(secondQuestionSummary.getQuestionHeaderText()));
    }
}
