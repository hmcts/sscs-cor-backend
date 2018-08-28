package uk.gov.hmcts.reform.sscscorbackend;

import java.util.ArrayList;
import java.util.List;
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
    protected IdamStub idamStub;

    private List<BaseStub> stubs = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        cohStub = new CohStub(cohUrl);
        stubs.add(cohStub);
        tokenGeneratorStub = new TokenGeneratorStub(tokenGeneratorUrl);
        stubs.add(tokenGeneratorStub);
        ccdStub = new CcdStub(ccdUrl);
        stubs.add(ccdStub);
        idamStub = new IdamStub(idamUrl, idamRedirectUrl, clientId, clientSecret);
        stubs.add(idamStub);
    }

    @After
    public void shutdownCoh() {
        stubs.forEach(BaseStub::shutdown);
    }
}
