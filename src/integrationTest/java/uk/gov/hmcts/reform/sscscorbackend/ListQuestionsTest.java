package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.equalTo;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class ListQuestionsTest extends BaseIntegrationTest {
    @Test
    public void getQuestion() {
        String hearingId = "1";
        String firstQuestionTitle = "first question";
        String secondQuestionTitle = "second question";
        cohStub.stubGetAllQuestionRounds(hearingId, firstQuestionTitle, secondQuestionTitle);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings/" + hearingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("questions[0].question_header_text", equalTo(firstQuestionTitle))
                .body("questions[1].question_header_text", equalTo(secondQuestionTitle));
    }
}
