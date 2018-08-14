package uk.gov.hmcts.reform.sscscorbackend;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OnlineHearingControllerTest {
    private static final String ONLINE_HEARING_ID = "Online hearing id";

    private CohStub cohStub;
    private TokenGeneratorStub tokenGeneratorStub;

    @Value("${coh.url}")
    private String cohUrl;
    @Value("${idam.s2s-auth.url}")
    private String tokenGeneratorUrl;
    @LocalServerPort
    private int applicationPort;


    @Before
    public void setUp() {
        cohStub = new CohStub(cohUrl);
        tokenGeneratorStub = new TokenGeneratorStub(tokenGeneratorUrl);
    }

    @After
    public void shutdownCoh() {
        if (cohStub != null) {
            cohStub.shutdown();
        }
        if (tokenGeneratorStub != null) {
            tokenGeneratorStub.shutdown();
        }
    }

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
                "    \"onlineHearingId\": \"string\",\n" +
                "    \"onlinePanel\": {\n" +
                    "    \"assignedTo\": \"string\",\n" +
                    "    \"medicalMember\": \"string\",\n" +
                    "    \"disabilityQualifiedMember\": \"string\"\n" +
                    "  }\n" +
                "  }\n" +
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
