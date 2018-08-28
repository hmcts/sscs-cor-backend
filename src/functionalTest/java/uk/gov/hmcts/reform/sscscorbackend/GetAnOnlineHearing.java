package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

@Ignore //todo re-enable this
public class GetAnOnlineHearing extends BaseFunctionTest {
    @Test
    public void getAnOnlineHearing() throws IOException {
        String caseId = UUID.randomUUID().toString();
        String expectedOnlineHearingId = cohRequests.createHearing(caseId);

        JSONObject onlineHearing = sscsCorBackendRequests.getOnlineHearing(caseId);
        String onlineHearingId = onlineHearing.getString("online_hearing_id");

        assertThat(onlineHearingId, is(expectedOnlineHearingId));
    }
}
