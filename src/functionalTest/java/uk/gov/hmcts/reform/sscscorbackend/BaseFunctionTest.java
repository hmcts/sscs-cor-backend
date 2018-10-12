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

public abstract class BaseFunctionTest {
    private final String baseUrl = System.getenv("TEST_URL");
    private String cohBaseUrl = "http://coh-cor-aat.service.core-compute-aat.internal";
    private CloseableHttpClient client;
    private HttpClient cohClient;

    protected final String decisionAward = "appeal-upheld";
    protected final String decisionHeader = "appeal-upheld";
    protected final String decisionReason = "Decision reason";
    protected final String decisionText = "Decision reason";

    protected SscsCorBackendRequests sscsCorBackendRequests;
    protected CohRequests cohRequests;

    @Before
    public void setUp() throws Exception {
        cohClient = buildClient("USE_COH_PROXY");
        client = buildClient("USE_BACKEND_PROXY");
        sscsCorBackendRequests = new SscsCorBackendRequests(baseUrl, client);
        cohRequests = new CohRequests(cohBaseUrl, cohClient);
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
        if (ccdCaseRequired) {
            emailAddress = createRandomEmail();
            String caseId = sscsCorBackendRequests.createCase(emailAddress);
            hearingId = cohRequests.createHearing(caseId);
        } else {
            hearingId = cohRequests.createHearing();
        }
        String questionId = cohRequests.createQuestion(hearingId);
        cohRequests.issueQuestionRound(hearingId);
        return new OnlineHearing(emailAddress, hearingId, questionId);
    }

    protected void answerQuestion(String hearingId, String questionId) throws IOException {
        cohRequests.createAnswer(hearingId, questionId, "Valid answer");
    }

    protected void createAndIssueDecision(String hearingId) throws IOException, InterruptedException {
        cohRequests.createDecision(hearingId, decisionAward, decisionHeader, decisionReason, decisionText);
        cohRequests.issueDecision(hearingId, decisionAward, decisionHeader, decisionReason, decisionText);
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
}
