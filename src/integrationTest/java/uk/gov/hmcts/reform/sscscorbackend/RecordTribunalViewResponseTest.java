package uk.gov.hmcts.reform.sscscorbackend;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecordTribunalViewResponseTest extends BaseIntegrationTest {

    @Test
    public void recordAcceptedResponse() {
        String hearingId = "1";
        String reply = "decision_accepted";
        String reason = "";
        cohStub.stubPostDecisionReply(hearingId, reply, reason);
        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .body("{\"reply\":\"" + reply + "\", \"reason\":\"" + reason + "\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .patch("/continuous-online-hearings/" + hearingId + "/tribunal-view")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void recordRejectedResponse() {
        String hearingId = "1";
        String reply = "decision_rejected";
        String reason = "Reasons for rejecting tribunal's view";
        cohStub.stubPostDecisionReply(hearingId, reply, reason);
        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .body("{\"reply\":\"" + reply + "\", \"reason\":\"" + reason + "\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .patch("/continuous-online-hearings/" + hearingId + "/tribunal-view")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void recordRejectedResponseWithoutReason() {
        String hearingId = "1";
        String reply = "decision_rejected";
        String reason = "";
        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .body("{\"reply\":\"" + reply + "\", \"reason\":\"" + reason + "\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .patch("/continuous-online-hearings/" + hearingId + "/tribunal-view")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
