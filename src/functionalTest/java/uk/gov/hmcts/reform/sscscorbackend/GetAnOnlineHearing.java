package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.client.methods.RequestBuilder.get;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;

@Ignore //todo re-enable this
public class GetAnOnlineHearing extends BaseFunctionTest {
    @Test
    public void getAnOnlineHearing() throws IOException {
        String caseId = UUID.randomUUID().toString();
        String expectedOnlineHearingId = createHearing(caseId);

        // for now pass in the case id will need to look this up in CCD from the email address eventually.
        JSONObject onlineHearing = getOnlineHearing(caseId);
        String onlineHearingId = onlineHearing.getString("online_hearing_id");

        assertThat(onlineHearingId, is(expectedOnlineHearingId));
    }

    private JSONObject getOnlineHearing(String emailAddress) throws IOException {
        HttpResponse getOnlineHearingResponse = client.execute(get(baseUrl + "/continuous-online-hearings?email=" + emailAddress)
                .build());

        assertThat(getOnlineHearingResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getOnlineHearingResponse.getEntity());

        return new JSONObject(responseBody);
    }
}
