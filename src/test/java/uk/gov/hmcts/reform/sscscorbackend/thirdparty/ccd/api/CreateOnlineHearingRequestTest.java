package uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CreateOnlineHearingRequest;


public class CreateOnlineHearingRequestTest {
    @Test
    public void testEquals() {
        String caseId = "caseId";
        CreateOnlineHearingRequest createOnlineHearingRequest =
                new CreateOnlineHearingRequest(caseId);

        assertThat("Caseid should match", createOnlineHearingRequest, is(new CreateOnlineHearingRequest(caseId)));
    }
}
