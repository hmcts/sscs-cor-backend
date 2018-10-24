package uk.gov.hmcts.reform.sscscorbackend;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohAnswers;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.submitted;

import io.restassured.RestAssured;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohQuestionReference;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuestionTest extends BaseIntegrationTest {

    private static final String QUESTION_HEADER = "Question header";
    private static final String QUESTION_BODY = "Question body";
    private static final String ANSWER_TEXT = "Answer text";
    private static final String ANSWER_STATUS = "draft";
    private static final Long CCD_CASE_ID = 123L;

    @Test
    public void getQuestion() {
        String hearingId = "1";
        String questionId = "1";
        cohStub.stubGetQuestion(hearingId, questionId, QUESTION_HEADER, QUESTION_BODY);
        ZonedDateTime answerDate = ZonedDateTime.now();
        cohStub.stubGetAnswer(hearingId, questionId, ANSWER_TEXT, UUID.randomUUID().toString(), "answer_drafted", answerDate);
        cohStub.stubGetOnlineHearing(CCD_CASE_ID, hearingId);
        String fileName = "someFileName.txt";
        ccdStub.stubFindCaseByCaseId(CCD_CASE_ID, questionId, fileName);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        Evidence expectedEvidence = new Evidence("http://exmple.com/document/1", fileName);
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(new QuestionMatcher(QUESTION_HEADER, QUESTION_BODY, ANSWER_TEXT, answerDate, singletonList(expectedEvidence)));
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
        cohStub.stubGetAnswer(hearingId, questionId, "old answer", answerId, "answer_drafted", ZonedDateTime.now());
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

    @Test
    public void submitAnAnswerToAQuestion() {
        String hearingId = "1";
        String questionId = "1";;
        String answerId = UUID.randomUUID().toString();
        String answer = "answer";
        cohStub.stubGetAnswer(hearingId, questionId, answer, answerId, "answer_drafted", ZonedDateTime.now());
        cohStub.stubUpdateAnswer(hearingId, questionId, answer, answerId, submitted);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .post("/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void extendQuestionRoundDeadline() {
        String hearingId = "1";
        cohStub.stubExtendQuestionRoundDeadline(hearingId);
        String deadlineExpiryDate = now().plusDays(7).format(ISO_LOCAL_DATE_TIME);
        CohQuestionReference questionSummary = new CohQuestionReference(
                "first-id", 1, "first question", deadlineExpiryDate, someCohAnswers("answer_drafted")
        );
        cohStub.stubGetAllQuestionRounds(hearingId, questionSummary);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .patch("/continuous-online-hearings/" + hearingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("deadline_expiry_date", equalTo(deadlineExpiryDate));
    }

    @Test
    public void extendQuestionRoundDeadlineFails() throws InterruptedException {
        String hearingId = "1";
        cohStub.stubCannotExtendQuestionRoundDeadline(hearingId);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .patch("/continuous-online-hearings/" + hearingId)
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }
}
