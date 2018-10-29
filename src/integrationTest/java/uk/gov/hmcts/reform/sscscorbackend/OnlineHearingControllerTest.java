package uk.gov.hmcts.reform.sscscorbackend;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class OnlineHearingControllerTest extends BaseIntegrationTest {
    private static final String ONLINE_HEARING_ID = "Online hearing id";

    @Test
    public void createOnlineHearing() {
        cohStub.stubPostOnlineHearing(ONLINE_HEARING_ID);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .contentType("application/json")
                .body(postOnlineHearingJson)
                .when()
                .post("/notify/onlineappeal")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("online_hearing_id", Matchers.is(ONLINE_HEARING_ID));
    }

    @Test
    public void get400WhenNoPanel() {
        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .contentType("application/json")
                .body(postOnlineHearingNoPanelJson)
                .when()
                .post("/notify/onlineappeal")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private static final String postOnlineHearingJson = "{\n" +
            "  \"case_details\": {\n" +
            "    \"id\": \"string\",\n" +
            "    \"case_data\": {\n" +
            "        \"onlineHearingId\": \"string\",\n" +
            "        \"hearingType\": \"cor\",\n" +
            "        \"onlinePanel\": {\n" +
            "            \"assignedTo\": \"string\",\n" +
            "            \"medicalMember\": \"string\",\n" +
            "            \"disabilityQualifiedMember\": \"string\"\n" +
            "        }\n" +
            "      }\n" +
            "  }\n" +
            "}";
    private static final String postOnlineHearingNoPanelJson = "{\n" +
            "  \"case_details\": {\n" +
            "    \"id\": \"string\",\n" +
            "    \"case_data\": {\n" +
            "    \"onlineHearingId\": \"string\",\n" +
            "  }\n" +
            "  }\n" +
            "}";
}
