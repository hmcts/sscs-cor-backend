package uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;




public class CreateOnlineHearingRequestTest {
    @Test
    public void testEquals() {
        List<PanelRequest> panel = new ArrayList<>();
        panel.add(new PanelRequest("judge",
                "my name", "judge"));

        String caseId = "caseId";
        CreateOnlineHearingRequest createOnlineHearingRequest =
                new CreateOnlineHearingRequest(caseId,
                        panel);

        assertThat("Caseid should match", createOnlineHearingRequest, is(new CreateOnlineHearingRequest(caseId, null)));
    }
}
