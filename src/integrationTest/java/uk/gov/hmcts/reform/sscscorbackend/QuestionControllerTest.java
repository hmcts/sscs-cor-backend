package uk.gov.hmcts.reform.sscscorbackend;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuestionControllerTest extends BaseIntegrationTest {

    private static final String QUESTION_HEADER = "Question header";
    private static final String QUESTION_BODY = "Question body";
    private static final String ANSWER_TEXT = "Answer text";

    @Test
    public void getQuestion() {
        String hearingId = "1";
        String questionId = "1";
        cohStub.stubGetQuestion(hearingId, questionId, QUESTION_HEADER, QUESTION_BODY);
        cohStub.stubGetAnswer(hearingId, questionId, ANSWER_TEXT);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(new QuestionMatcher(QUESTION_HEADER, QUESTION_BODY, ANSWER_TEXT));
    }

    @Test
    public void answerAQuestion() {
        String hearingId = "1";
        String questionId = "1";
        String newAnswer = "an answer";
        cohStub.stubCannotFindAnswers(hearingId, questionId);
        cohStub.stubCreateAnswer(hearingId, questionId, newAnswer);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .body("{\"answer\":\"" + newAnswer + "\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .put("/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void updateAnAnswerToAQuestion() {
        String hearingId = "1";
        String questionId = "1";
        String newAnswer = "new answer";
        String answerId = UUID.randomUUID().toString();
        cohStub.stubGetAnswer(hearingId, questionId, "old answer", answerId);
        cohStub.stubUpdateAnswer(hearingId, questionId, newAnswer, answerId);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .body("{\"answer\":\"" + newAnswer + "\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .put("/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void get404WhenQuestionDoesNotExist() {
        String hearingId = "2";
        String questionId = "2";
        cohStub.stubCannotFindQuestion(hearingId, questionId);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
