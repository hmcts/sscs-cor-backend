package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.idam.IdamService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "idam.s2s-auth.url=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal",
        "idam.url=https://preprod-idamapi.reform.hmcts.net:3511",
        "idam.oauth2.redirectUrl=https://evidence-sharing-preprod.sscs.reform.hmcts.net",
        "coh.url=http://coh-cor-aat.service.core-compute-aat.internal"
})
public abstract class BaseFunctionTest {
    private final String baseUrl = System.getenv("TEST_URL");
    private String cohBaseUrl = "http://coh-cor-aat.service.core-compute-aat.internal";
    private CloseableHttpClient client;
    private HttpClient cohClient;

    protected final String decisionAward = "appeal-upheld";
    protected final String decisionHeader = "appeal-upheld";
    protected final String decisionReason = "Decision reason";
    protected final String decisionText = "{\\\"decisions_SSCS_benefit_{case_id}\\\":{\\\"preliminaryView\\\":\\\"yes\\\",\\\"visitedPages\\\":{\\\"create\\\":true,\\\"preliminary-advanced\\\":true,\\\"set-award-dates\\\":true,\\\"scores\\\":true,\\\"budgeting-decisions\\\":true,\\\"planning-journeys\\\":true},\\\"forDailyLiving\\\":\\\"noAward\\\",\\\"forMobility\\\":\\\"enhancedRate\\\",\\\"compareToDWPAward\\\":\\\"Higher\\\",\\\"awardEndDateDay\\\":\\\"11\\\",\\\"awardEndDateMonth\\\":\\\"12\\\",\\\"awardEndDateYear\\\":\\\"2018\\\",\\\"approveDraftConsent\\\":\\\"indefinite\\\",\\\"preparingFood\\\":false,\\\"takingNutrition\\\":false,\\\"managingTherapy\\\":false,\\\"washingBathing\\\":false,\\\"managingToilet\\\":false,\\\"dressingUndressing\\\":false,\\\"communicatingVerbally\\\":false,\\\"readingAndUnderstanding\\\":false,\\\"engagingWithOtherPeople\\\":false,\\\"makingBudgetingDecisions\\\":true,\\\"planningFollowingJourneys\\\":true,\\\"movingAround\\\":false,\\\"dailyLivingMakingBudgetDecisions\\\":\\\"6\\\",\\\"MobilityPlanningJourneys\\\":\\\"12\\\",\\\"reasonsTribunalView\\\":\\\"There was a reason!\\\",\\\"awardStartDateDay\\\":\\\"1\\\",\\\"awardStartDateMonth\\\":\\\"4\\\",\\\"awardStartDateYear\\\":\\\"2017\\\"}}";

    protected SscsCorBackendRequests sscsCorBackendRequests;
    protected CohRequests cohRequests;

    @Autowired
    private IdamService idamService;

    @Before
    public void setUp() throws Exception {
        cohClient = buildClient("USE_COH_PROXY");
        client = buildClient("USE_BACKEND_PROXY");
        sscsCorBackendRequests = new SscsCorBackendRequests(baseUrl, client);
        cohRequests = new CohRequests(idamService, cohBaseUrl, cohClient);
    }

    protected String createRandomEmail() {
        int randomNumber = (int) (Math.random() * 10000000);
        String emailAddress = "test" + randomNumber + "@hmcts.net";
        System.out.println("emailAddress " + emailAddress);
        return emailAddress;
    }

    protected OnlineHearing createHearingWithQuestion(boolean ccdCaseRequired) throws IOException, InterruptedException {
        String hearingId;
        String emailAddress = null;
        String caseId = null;
        if (ccdCaseRequired) {
            emailAddress = createRandomEmail();
            caseId = sscsCorBackendRequests.createCase(emailAddress);
            hearingId = cohRequests.createHearing(caseId);
        } else {
            hearingId = cohRequests.createHearing();
        }
        String questionId = cohRequests.createQuestion(hearingId);
        cohRequests.issueQuestionRound(hearingId);
        return new OnlineHearing(emailAddress, hearingId, questionId, caseId);
    }

    protected void answerQuestion(String hearingId, String questionId) throws IOException {
        cohRequests.createAnswer(hearingId, questionId, "Valid answer");
    }

    protected void createAndIssueDecision(String hearingId, String caseId) throws IOException, InterruptedException {
        String decisionTextJson = decisionText.replace("{case_id}", caseId);
        cohRequests.createDecision(hearingId, decisionAward, decisionHeader, decisionReason, decisionTextJson);
        cohRequests.issueDecision(hearingId, decisionAward, decisionHeader, decisionReason, decisionTextJson);
    }

    protected void recordAppellantViewResponse(String hearingId, String reply, String reason) throws IOException {
        cohRequests.addDecisionReply(hearingId, reply, reason);
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

    protected void resolveHearing(String hearingId, String caseId) throws IOException {
        sscsCorBackendRequests.triggerResolve(hearingId, caseId);
    }
}
