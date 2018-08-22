package uk.gov.hmcts.reform.sscscorbackend;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    @Value("${coh.url}")
    private String cohUrl;
    @Value("${idam.s2s-auth.url}")
    private String tokenGeneratorUrl;
    @Value("${idam.url}")
    private String idamUrl;
    @Value("${core_case_data.api.url}")
    private String ccdUrl;

    @Value("${idam.oauth2.redirectUrl}")
    private String idamRedirectUrl;
    @Value("${idam.oauth2.client.id}")
    private String clientId;
    @Value("${idam.oauth2.client.secret}")
    private String clientSecret;

    @LocalServerPort
    protected int applicationPort;

    protected CohStub cohStub;
    protected TokenGeneratorStub tokenGeneratorStub;
    protected CcdStub ccdStub;
    protected TokenGeneratorStub idamStub;

    @Before
    public void setUp() throws Exception {
        cohStub = new CohStub(cohUrl);
        tokenGeneratorStub = new TokenGeneratorStub(tokenGeneratorUrl, idamRedirectUrl, clientId, clientSecret);
        ccdStub = new CcdStub(ccdUrl);
        idamStub = new TokenGeneratorStub(idamUrl, idamRedirectUrl, clientId, clientSecret);
    }

    @After
    public void shutdownCoh() {
        if (cohStub != null) {
            cohStub.printAllRequests();
            cohStub.shutdown();
        }
        if (tokenGeneratorStub != null) {
            tokenGeneratorStub.printAllRequests();
            tokenGeneratorStub.shutdown();
        }
        if (ccdStub != null) {
            ccdStub.printAllRequests();
            ccdStub.shutdown();
        }
        if (idamStub != null) {
            idamStub.printAllRequests();
            idamStub.shutdown();
        }
    }
}
