package uk.gov.hmcts.reform.sscscorbackend;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscscorbackend.service.CohService;

// Need to set the following to use
// IDAM_S2S_AUTH
// IDAM.S2S-AUTH.TOTP_SECRET
// COH_URL

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class)
public class SecurityTest {
    @Autowired
    private CohService cohService;

    @Test
    public void testConnectToCoh() {
        System.out.println(cohService.getOnlineHearing("9784494d-b95d-4510-bfad-0d8c73501c50"));
    }
}
