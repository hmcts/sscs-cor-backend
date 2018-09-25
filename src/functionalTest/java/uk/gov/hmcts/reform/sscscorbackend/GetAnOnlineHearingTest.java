package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Test;

public class GetAnOnlineHearingTest extends BaseFunctionTest {
    @Test
    public void getAnOnlineHearing() throws IOException {
        String emailAddress = "someEmail" + UUID.randomUUID().toString() + "@example.com";
        System.out.println("emailAddress " + emailAddress);
        String caseId = sscsCorBackendRequests.createCase(emailAddress);
        String expectedOnlineHearingId = cohRequests.createHearing(caseId);

        JSONObject onlineHearing = sscsCorBackendRequests.getOnlineHearing(emailAddress);
        String onlineHearingId = onlineHearing.getString("online_hearing_id");

        assertThat(onlineHearingId, is(expectedOnlineHearingId));
    }
}
