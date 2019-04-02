package uk.gov.hmcts.reform.sscscorbackend;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class OnlineHearingControllerTest extends BaseIntegrationTest {
    private static final String ONLINE_HEARING_ID = "Online hearing id";

    @Test
    public void createOnlineHearing() throws JsonProcessingException {
        cohStub.stubPostOnlineHearing(ONLINE_HEARING_ID);
        ccdStub.stubAddUserToCase(123456, "medical");
        ccdStub.stubAddUserToCase(123456, "disability");

        getRequest()
                .contentType("application/json")
                .body(postOnlineHearingJson)
                .when()
                .post("/notify/onlineappeal")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("onlineHearingCreated", Matchers.is(true));
    }

    @Test
    public void get400WhenNoPanel() {
        getRequest()
                .contentType("application/json")
                .body(postOnlineHearingNoPanelJson)
                .when()
                .post("/notify/onlineappeal")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private static final String postOnlineHearingJson = "{\n" +
            "  \"case_details\": {\n" +
            "    \"id\": \"123456\",\n" +
            "    \"case_data\": {\n" +
            "        \"onlineHearingId\": \"string\",\n" +
            "        \"hearingType\": \"cor\",\n" +
            "        \"assignedToJudge\": \"judge\",\n" +
            "        \"assignedToDisabilityMember\": \"disability\",\n" +
            "        \"assignedToMedicalMember\": \"medical\"\n" +
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
