package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.client.methods.RequestBuilder.get;
import static org.apache.http.client.methods.RequestBuilder.post;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class GetAQuestion {

    private final String baseUrl = System.getenv("TEST_URL");
    // private final String baseUrl = "http://sscs-cor-backend-aat-staging.service.core-compute-aat.internal";
    private String cohBaseUrl = "http://coh-cor-aat.service.core-compute-aat.internal";

    @Test
    public void getsAQuestion() throws IOException,
            KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(builder.build(), ALLOW_ALL_HOSTNAME_VERIFIER);

        HttpClient client = HttpClientBuilder.create()
                .setSSLSocketFactory(sslsf)
                // .setProxy(new HttpHost("proxyout.reform.hmcts.net", 8080))
                .build();

        String hearingId = createHearing(client);
        String questionId = createQuestion(client, hearingId);

        HttpResponse httpResponse3 = client.execute(get(baseUrl + "/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .setHeader(HttpHeaders.AUTHORIZATION, "someValue")
                .setHeader("ServiceAuthorization", "someValue")
                .build());

        assertThat(httpResponse3.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String questionBodyText = getFromJsonBody(httpResponse3, "question_body_text");

        assertThat(questionBodyText, is("question text"));
    }

    private String createHearing(HttpClient client) throws IOException {
        String hearingId = makePostRequest(client, cohBaseUrl + "/continuous-online-hearings", "{\n" +
                "  \"case_id\": \"" + UUID.randomUUID().toString() + "\",\n" +
                "  \"jurisdiction\": \"SSCS\",\n" +
                "  \"panel\": [\n" +
                "    {\n" +
                "      \"identity_token\": \"Judge\",\n" +
                "      \"name\": \"John Dead\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"start_date\": \"2018-08-09T13:14:45.178Z\",\n" +
                "  \"state\": \"NEW\"\n" +
                "}", "online_hearing_id");
        System.out.println("Hearing id " + hearingId);
        return hearingId;
    }

    private String createQuestion(HttpClient client, String hearingId) throws IOException {
        String questionId = makePostRequest(client, cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/questions",
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

    private String makePostRequest(HttpClient client, String uri, String body, String responseValue) throws IOException {
        HttpResponse httpResponse = client.execute(post(uri)
                .setHeader(HttpHeaders.AUTHORIZATION, "someValue")
                .setHeader("ServiceAuthorization", "someValue")
                .setEntity(new StringEntity(body, org.apache.http.entity.ContentType.APPLICATION_JSON))
                .build());

        assertThat(httpResponse.getStatusLine().getStatusCode(), is(HttpStatus.CREATED.value()));
        return getFromJsonBody(httpResponse, responseValue);
    }

    private String getFromJsonBody(HttpResponse httpResponse, String responseValue) throws IOException {
        String responseBody = EntityUtils.toString(httpResponse.getEntity());

        JSONObject obj = new JSONObject(responseBody);
        return obj.getString(responseValue);
    }
}