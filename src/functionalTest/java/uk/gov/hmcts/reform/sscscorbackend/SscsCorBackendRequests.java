package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.client.methods.RequestBuilder.get;
import static org.apache.http.client.methods.RequestBuilder.post;
import static org.apache.http.client.methods.RequestBuilder.put;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public class SscsCorBackendRequests {
    private String baseUrl;
    private CloseableHttpClient client;

    public SscsCorBackendRequests(String baseUrl, CloseableHttpClient client) {
        this.baseUrl = baseUrl;
        this.client = client;
    }

    public JSONObject getQuestion(String hearingId, String questionId) throws IOException {
        HttpResponse getQuestionResponse = client.execute(get(baseUrl + "/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .build());

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getQuestionResponse.getEntity());

        return new JSONObject(responseBody);
    }

    public JSONObject getQuestions(String hearingId) throws IOException {
        HttpResponse getQuestionResponse = client.execute(get(baseUrl + "/continuous-online-hearings/" + hearingId)
                .build());

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getQuestionResponse.getEntity());

        return new JSONObject(responseBody);
    }

    public void answerQuestion(String hearingId, String questionId, String answer) throws IOException {
        HttpResponse getQuestionResponse = client.execute(put(baseUrl + "/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .setEntity(new StringEntity("{\"answer\":\"" + answer + "\"}", APPLICATION_JSON))
                .build());

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    public void submitAnswer(String hearingId, String questionId) throws IOException {
        HttpResponse getQuestionResponse = client.execute(post(baseUrl + "/continuous-online-hearings/" + hearingId + "/questions/" + questionId)
                .setHeader("Content-Length", "0")
                .build());

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    public JSONObject getOnlineHearing(String emailAddress) throws IOException {
        HttpResponse getOnlineHearingResponse = client.execute(get(baseUrl + "/continuous-online-hearings?email=" + emailAddress)
                .build());

        assertThat(getOnlineHearingResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getOnlineHearingResponse.getEntity());

        return new JSONObject(responseBody);
    }
}
