package uk.gov.hmcts.reform.sscscorbackend;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.hamcrest.CoreMatchers.equalTo;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohAnswers;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohQuestionReference;

public class ListQuestionsTest extends BaseIntegrationTest {
    @Test
    public void getQuestion() {
        String hearingId = "1";
        String deadlineExpiryDate = now().plusDays(7).format(ISO_LOCAL_DATE_TIME);
        CohQuestionReference firstQuestionSummary = new CohQuestionReference(
                "first-id", 1, "first question", deadlineExpiryDate, someCohAnswers("answer_drafted")
        );
        CohQuestionReference secondQuestionSummary = new CohQuestionReference(
                "second-id", 2, "second question", deadlineExpiryDate, someCohAnswers("answer_submitted")
        );
        cohStub.stubGetAllQuestionRounds(hearingId, firstQuestionSummary, secondQuestionSummary);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings/" + hearingId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("deadline_expiry_date", equalTo(deadlineExpiryDate))
                .body("questions[0].question_id", equalTo(firstQuestionSummary.getQuestionId()))
                .body("questions[0].question_header_text", equalTo(firstQuestionSummary.getQuestionHeaderText()))
                .body("questions[0].answer_state", equalTo("draft"))
                .body("questions[1].question_id", equalTo(secondQuestionSummary.getQuestionId()))
                .body("questions[1].question_header_text", equalTo(secondQuestionSummary.getQuestionHeaderText()))
                .body("questions[1].answer_state", equalTo("submitted"));
    }
}
