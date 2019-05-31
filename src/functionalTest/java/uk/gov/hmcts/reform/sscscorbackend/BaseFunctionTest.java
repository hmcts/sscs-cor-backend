package uk.gov.hmcts.reform.sscscorbackend;

import static java.lang.Long.valueOf;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;
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
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "idam.s2s-auth.url=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal",
        "idam.url=http://idam-api.aat.platform.hmcts.net",
        "idam.oauth2.redirectUrl=https://evidence-sharing-preprod.sscs.reform.hmcts.net",
        "coh.url=http://coh-cor-aat.service.core-compute-aat.internal",
        "core_case_data.api.url=http://ccd-data-store-api-aat.service.core-compute-aat.internal"
})
public abstract class BaseFunctionTest {
    private final String baseUrl = System.getenv("TEST_URL") != null ? System.getenv("TEST_URL") : "http://localhost:8090";

    private String cohBaseUrl = "http://coh-cor-aat.service.core-compute-aat.internal";
    private CloseableHttpClient client;
    private HttpClient cohClient;

    protected final String decisionAward = "appeal-upheld";
    protected final String decisionHeader = "appeal-upheld";
    protected final String decisionReason = "Decision reason";
    protected final String decisionText = "{\\\"decisions_SSCS_benefit_{case_id}\\\":{\\\"preliminaryView\\\":\\\"yes\\\",\\\"visitedPages\\\":{\\\"create\\\":true,\\\"preliminary-advanced\\\":true,\\\"set-award-dates\\\":true,\\\"scores\\\":true,\\\"budgeting-decisions\\\":true,\\\"planning-journeys\\\":true},\\\"forDailyLiving\\\":\\\"noAward\\\",\\\"forMobility\\\":\\\"enhancedRate\\\",\\\"compareToDWPAward\\\":\\\"Higher\\\",\\\"awardEndDateDay\\\":\\\"11\\\",\\\"awardEndDateMonth\\\":\\\"12\\\",\\\"awardEndDateYear\\\":\\\"2018\\\",\\\"endDateRadio\\\":\\\"indefinite\\\",\\\"preparingFood\\\":false,\\\"takingNutrition\\\":false,\\\"managingTherapy\\\":false,\\\"washingBathing\\\":false,\\\"managingToilet\\\":false,\\\"dressingUndressing\\\":false,\\\"communicatingVerbally\\\":false,\\\"readingAndUnderstanding\\\":false,\\\"engagingWithOtherPeople\\\":false,\\\"makingBudgetingDecisions\\\":true,\\\"planningFollowingJourneys\\\":true,\\\"movingAround\\\":false,\\\"dailyLivingMakingBudgetDecisions\\\":\\\"6\\\",\\\"MobilityPlanningJourneys\\\":\\\"12\\\",\\\"reasonsTribunalView\\\":\\\"There was \\\\n a reason!\\\",\\\"awardStartDateDay\\\":\\\"1\\\",\\\"awardStartDateMonth\\\":\\\"4\\\",\\\"awardStartDateYear\\\":\\\"2017\\\"}}";

    protected SscsCorBackendRequests sscsCorBackendRequests;
    protected CohRequests cohRequests;

    @Autowired
    private IdamService idamService;
    @Autowired
    protected CorCcdService corCcdService;

    @Before
    public void setUp() throws Exception {
        cohClient = buildClient("USE_COH_PROXY");
        client = buildClient("USE_BACKEND_PROXY");
        sscsCorBackendRequests = new SscsCorBackendRequests(idamService, baseUrl, client);
        cohRequests = new CohRequests(idamService, cohBaseUrl, cohClient);
    }

    protected String createRandomEmail() {
        int randomNumber = (int) (Math.random() * 10000000);
        String emailAddress = "test" + randomNumber + "@hmcts.net";
        System.out.println("emailAddress " + emailAddress);
        return emailAddress;
    }

    protected OnlineHearing createHearing(boolean ccdCaseRequired) throws IOException {
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
        return new OnlineHearing(emailAddress, hearingId, null, caseId);
    }

    protected OnlineHearing createHearingWithQuestion(boolean ccdCaseRequired) throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearing(ccdCaseRequired);
        String questionId = cohRequests.createQuestion(onlineHearing.getHearingId());
        cohRequests.issueQuestionRound(onlineHearing.getHearingId());
        return new OnlineHearing(onlineHearing.getEmailAddress(), onlineHearing.getHearingId(), questionId, onlineHearing.getCaseId());
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

    protected void relistHearing(String hearingId, String caseId) throws IOException {
        sscsCorBackendRequests.cohHearingRelisted(hearingId, caseId);
    }

    protected void decisionIssued(String hearingId, String caseId) throws IOException {
        sscsCorBackendRequests.cohDecisionIssued(hearingId, caseId);
    }

    protected SscsCaseDetails getCaseDetails(String caseId) {
        return corCcdService.getByCaseId(valueOf(caseId), idamService.getIdamTokens());
    }

    protected void waitForCcdEvent(String caseId, EventType eventType) throws InterruptedException {
        waitUntil(() -> corCcdService.getHistoryEvents(valueOf(caseId)).stream()
                        .anyMatch(event -> EventType.COH_ONLINE_HEARING_RELISTED.equals(event.getEventType())),
                10,
                "CCD does not have [" + eventType.name() + "] event for case [" + caseId + "]"
        );
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
