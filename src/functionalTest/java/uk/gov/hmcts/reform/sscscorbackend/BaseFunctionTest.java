package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.client.methods.RequestBuilder.post;
import static org.apache.http.client.methods.RequestBuilder.put;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public abstract class BaseFunctionTest {
    protected final String baseUrl = System.getenv("TEST_URL");
    protected CloseableHttpClient client;
    // private final String baseUrl = "http://sscs-cor-backend-aat-staging.service.core-compute-aat.internal";
    private String cohBaseUrl = "http://coh-cor-aat.service.core-compute-aat.internal";
    private HttpClient cohClient;

    private static String makePostRequest(HttpClient client, String uri, String body, String responseValue) throws IOException {
        HttpResponse httpResponse = client.execute(post(uri)
                .setHeader(HttpHeaders.AUTHORIZATION, "someValue")
                .setHeader("ServiceAuthorization", "someValue")
                .setEntity(new StringEntity(body, APPLICATION_JSON))
                .build());

        assertThat(httpResponse.getStatusLine().getStatusCode(), is(HttpStatus.CREATED.value()));
        String responseBody = EntityUtils.toString(httpResponse.getEntity());

        return new JSONObject(responseBody).getString(responseValue);
    }

    private static void makePutRequest(HttpClient client, String uri, String body) throws IOException {
        HttpResponse httpResponse = client.execute(put(uri)
                .setHeader(HttpHeaders.AUTHORIZATION, "someValue")
                .setHeader("ServiceAuthorization", "someValue")
                .setEntity(new StringEntity(body, APPLICATION_JSON))
                .build());

        assertThat(httpResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Before
    public void setUp() throws Exception {
        cohClient = buildClient("USE_COH_PROXY");
        client = buildClient("USE_BACKEND_PROXY");
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

    protected void answerQuestion(String hearingId, String questionId, String answer) throws IOException {
        HttpResponse getQuestionResponse = client.execute(put(baseUrl + "/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .setEntity(new StringEntity("{\"answer\":\"" + answer + "\", \"answer_state\":\"draft\"}", APPLICATION_JSON))
                .build());

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    protected String createHearing() throws IOException {
        String hearingId = makePostRequest(cohClient, cohBaseUrl + "/continuous-online-hearings", "{\n" +
                "  \"case_id\": \"" + UUID.randomUUID().toString() + "\",\n" +
                "  \"jurisdiction\": \"SSCS\",\n" +
                "  \"panel\": [\n" +
                "    {\n" +
                "      \"identity_token\": \"Judge\",\n" +
                "      \"name\": \"John Dead\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"start_date\": \"2018-08-09T13:14:45.178Z\",\n" +
                "  \"state\": \"continuous_online_hearing_started\"\n" +
                "}", "online_hearing_id");
        System.out.println("Hearing id " + hearingId);
        return hearingId;
    }

    protected String createQuestion(String hearingId) throws IOException {
        String questionId = makePostRequest(cohClient, cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/questions",
                "{\n" +
                        "  \"owner_reference\": \"owner_ref\",\n" +
                        "  \"question_body_text\": \"question text\",\n" +
                        "  \"question_header_text\": \"question header\",\n" +
                        "  \"question_ordinal\": \"1\",\n" +
                        "  \"question_round\": \"1\"\n" +
                        "}",
                "question_id");
        System.out.println("Question id " + questionId);
        return questionId;
    }

    protected void issueQuestionRound(String hearingId) throws IOException {
        makePutRequest(cohClient, cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/questionrounds/1",
                "{\"state_name\": \"question_issue_pending\"}"
        );
    }
}
