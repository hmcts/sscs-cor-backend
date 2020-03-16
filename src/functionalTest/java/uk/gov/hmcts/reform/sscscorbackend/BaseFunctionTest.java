package uk.gov.hmcts.reform.sscscorbackend;

import static java.lang.Long.valueOf;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@TestPropertySource(properties = {
        "idam.s2s-auth.url=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal",
        "idam.url=https://idam-api.aat.platform.hmcts.net",
        "idam.oauth2.redirectUrl=https://evidence-sharing-preprod.sscs.reform.hmcts.net",
        "core_case_data.api.url=http://ccd-data-store-api-aat.service.core-compute-aat.internal"
})
@Slf4j
public abstract class BaseFunctionTest {
    private final String baseUrl = System.getenv("TEST_URL") != null ? System.getenv("TEST_URL") : "http://localhost:8090";

    private CloseableHttpClient client;
    private HttpClient cohClient;

    protected final String decisionAward = "appeal-upheld";
    protected final String decisionHeader = "appeal-upheld";
    protected final String decisionReason = "Decision reason";
    protected final String decisionText = "{\\\"decisions_SSCS_benefit_{case_id}\\\":{\\\"preliminaryView\\\":\\\"yes\\\",\\\"visitedPages\\\":{\\\"create\\\":true,\\\"preliminary-advanced\\\":true,\\\"set-award-dates\\\":true,\\\"scores\\\":true,\\\"budgeting-decisions\\\":true,\\\"planning-journeys\\\":true},\\\"forDailyLiving\\\":\\\"noAward\\\",\\\"forMobility\\\":\\\"enhancedRate\\\",\\\"compareToDWPAward\\\":\\\"Higher\\\",\\\"awardEndDateDay\\\":\\\"11\\\",\\\"awardEndDateMonth\\\":\\\"12\\\",\\\"awardEndDateYear\\\":\\\"2018\\\",\\\"endDateRadio\\\":\\\"indefinite\\\",\\\"preparingFood\\\":false,\\\"takingNutrition\\\":false,\\\"managingTherapy\\\":false,\\\"washingBathing\\\":false,\\\"managingToilet\\\":false,\\\"dressingUndressing\\\":false,\\\"communicatingVerbally\\\":false,\\\"readingAndUnderstanding\\\":false,\\\"engagingWithOtherPeople\\\":false,\\\"makingBudgetingDecisions\\\":true,\\\"planningFollowingJourneys\\\":true,\\\"movingAround\\\":false,\\\"dailyLivingMakingBudgetDecisions\\\":\\\"6\\\",\\\"MobilityPlanningJourneys\\\":\\\"12\\\",\\\"reasonsTribunalView\\\":\\\"There was \\\\n a reason!\\\",\\\"awardStartDateDay\\\":\\\"1\\\",\\\"awardStartDateMonth\\\":\\\"4\\\",\\\"awardStartDateYear\\\":\\\"2017\\\"}}";

    protected SscsCorBackendRequests sscsCorBackendRequests;

    @Autowired
    private IdamService idamService;
    @Autowired
    protected CorCcdService corCcdService;

    @Value("${idam.url}")
    private String idamApiUrl;
    protected IdamTestApiRequests idamTestApiRequests;

    @Before
    public void setUp() throws Exception {
        client = buildClient("USE_BACKEND_PROXY");
        sscsCorBackendRequests = new SscsCorBackendRequests(idamService, baseUrl, client);
        idamTestApiRequests = new IdamTestApiRequests(cohClient, idamApiUrl);
    }

    protected String createRandomEmail() {
        int randomNumber = (int) (Math.random() * 10000000);
        String emailAddress = "test" + randomNumber + "@hmcts.net";
        log.info("emailAddress " + emailAddress);
        return emailAddress;
    }

    protected CreatedCcdCase createCase() throws IOException {
        String emailAddress = createRandomEmail();

        CreatedCcdCase createdCcdCase = null;
        createdCcdCase = sscsCorBackendRequests.createOralCase(emailAddress);

        return createdCcdCase;
    }
    
    protected CreatedCcdCase createCcdCase(String emailAddress) throws IOException {
        String hearingId;
        CreatedCcdCase createdCcdCase = sscsCorBackendRequests.createCase(emailAddress);
        System.out.println("Case id " + createdCcdCase.getCaseId());
        return createdCcdCase;
    }

    public static class CreatedCaseHearingData {
        private final OnlineHearing onlineHearing;
        private final CreatedCcdCase createdCcdCase;

        public CreatedCaseHearingData(OnlineHearing onlineHearing, CreatedCcdCase createdCcdCase) {
            this.onlineHearing = onlineHearing;
            this.createdCcdCase = createdCcdCase;
        }

        public OnlineHearing getOnlineHearing() {
            return onlineHearing;
        }

        public CreatedCcdCase getCreatedCcdCase() {
            return createdCcdCase;
        }
    }

    private CloseableHttpClient buildClient(String proxySystemProperty) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(builder.build(), ALLOW_ALL_HOSTNAME_VERIFIER);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .setSSLSocketFactory(sslsf);
        if (System.getenv(proxySystemProperty) != null) {
            httpClientBuilder = httpClientBuilder.setProxy(new HttpHost("proxyout.reform.hmcts.net", 8080));
        }
        return httpClientBuilder.build();
    }

    protected SscsCaseDetails getCaseDetails(String caseId) {
        return corCcdService.getByCaseId(valueOf(caseId), idamService.getIdamTokens());
    }

    public static void waitUntil(Supplier<Boolean> condition, long timeoutInSeconds, String timeoutMessage) throws InterruptedException {
        long timeout = timeoutInSeconds * 1000L * 1000000L;
        long startTime = System.nanoTime();
        while (true) {
            if (condition.get()) {
                break;
            } else if (System.nanoTime() - startTime >= timeout) {
                throw new RuntimeException(timeoutMessage);
            }
            Thread.sleep(1000L);
        }
    }
}
