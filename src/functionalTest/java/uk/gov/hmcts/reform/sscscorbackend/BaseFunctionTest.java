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
    protected final String decisionTextRequest = "{\\\"dailyLivingRate\\\": \\\"Standard rate\\\",\\\"mobilityRate\\\": \\\"No award\\\"," +
            "\\\"reasonsForView\\\": \\\"We considered all the evidence you and DWP submitted in relation to your appeal. " +
            "This includes any additional evidence you submitted./nAfter considering this evidence we acknowledge that you " +
            "experience pain in your lower back when doing some tasks around the house. Specifically washing yourself and " +
            "preparing food. We consider that this pain does not hinder you enough to be awarded the enhanced rate of " +
            "the daily living component of PIP. It is therefore our view that you are eligible for Daily living at " +
            "the standard rate.\\\"," +
            "\\\"dailyLivingActivities\\\": [{" +
            "\\\"activity\\\": \\\"Preparing food\\\"," +
            "\\\"descriptor\\\": \\\"Needs to use an aid or appliance to be able to prepare or cook a simple meal\\\"," +
            "\\\"points\\\": \\\"2\\\"}, {\\\"activity\\\": \\\"Washing and bathing\\\"," +
            "\\\"descriptor\\\": \\\"Needs to use an aid or appliance to be able to wash or bathe\\\"," +
            "\\\"points\\\": \\\"2\\\" }]," +
            "\\\"mobilityActivities\\\": [{" +
            "\\\"activity\\\": \\\"Planning and following journeys\\\"," +
            "\\\"descriptor\\\": \\\"Needs prompting to be able to undertake any journey to avoid overwhelming psychological distress to the claimant.\\\"," +
            "\\\"points\\\": 4\\\"},{" +
            "\\\"activity\\\": \\\"Moving around\\\"," +
            "\\\"descriptor\\\": \\\"Can stand and then move more than 50 metres but no more than 200 metres, either aided or unaided\\\"," +
            "\\\"points\\\": 4 }] }";
    protected final String decisionTextResponse = "{\"dailyLivingRate\": \"Standard rate\",\"mobilityRate\": \"No award\"," +
            "\"reasonsForView\": \"We considered all the evidence you and DWP submitted in relation to your appeal. " +
            "This includes any additional evidence you submitted./nAfter considering this evidence we acknowledge that you " +
            "experience pain in your lower back when doing some tasks around the house. Specifically washing yourself and " +
            "preparing food. We consider that this pain does not hinder you enough to be awarded the enhanced rate of " +
            "the daily living component of PIP. It is therefore our view that you are eligible for Daily living at " +
            "the standard rate.\"," +
            "\"dailyLivingActivities\": [{" +
            "\"activity\": \"Preparing food\"," +
            "\"descriptor\": \"Needs to use an aid or appliance to be able to prepare or cook a simple meal\"," +
            "\"points\": \"2\"}, {\"activity\": \"Washing and bathing\"," +
            "\"descriptor\": \"Needs to use an aid or appliance to be able to wash or bathe\"," +
            "\"points\": \"2\" }]," +
            "\"mobilityActivities\": [{" +
            "\"activity\": \"Planning and following journeys\"," +
            "\"descriptor\": \"Needs prompting to be able to undertake any journey to avoid overwhelming psychological distress to the claimant.\"," +
            "\"points\": 4\"},{" +
            "\"activity\": \"Moving around\"," +
            "\"descriptor\": \"Can stand and then move more than 50 metres but no more than 200 metres, either aided or unaided\"," +
            "\"points\": 4 }] }";

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

    protected void createAndIssueDecision(String hearingId) throws IOException, InterruptedException {
        cohRequests.createDecision(hearingId, decisionAward, decisionHeader, decisionReason, decisionTextRequest);
        cohRequests.issueDecision(hearingId, decisionAward, decisionHeader, decisionReason, decisionTextRequest);
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
