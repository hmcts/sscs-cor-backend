package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class CitizenLoginTest extends BaseFunctionTest {
    @Test
    public void checkUserDoesNotHaveCaseAssignCaseAndCheckUserHasCase() throws IOException {
        String userEmail = createRandomEmail();
        idamTestApiRequests.createUser(userEmail);
        CreatedCcdCase ccdCase = createCcdCase(userEmail);

        String appellantTya = ccdCase.getAppellantTya();

        JSONArray onlineHearingForTya = sscsCorBackendRequests.getOnlineHearingForCitizen(appellantTya, userEmail);
        assertThat(onlineHearingForTya.length(), is(0));

        JSONObject jsonObject = sscsCorBackendRequests.assignCaseToUser(appellantTya, userEmail, "TN32 6PL");
        Long expectedCaseId = Long.valueOf(ccdCase.getCaseId());
        assertThat(jsonObject.getLong("case_id"), is(expectedCaseId));

        onlineHearingForTya = sscsCorBackendRequests.getOnlineHearingForCitizen("", userEmail);
        assertThat(onlineHearingForTya.length(), is(1));
        assertThat(onlineHearingForTya.getJSONObject(0).get("case_id"), is(expectedCaseId));
    }
}
