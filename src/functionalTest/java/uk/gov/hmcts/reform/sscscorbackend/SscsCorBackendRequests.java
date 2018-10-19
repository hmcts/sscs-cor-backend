package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.client.methods.RequestBuilder.*;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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

    public String createCase(String emailAddress) throws IOException {
        HttpResponse createCaseResponse = client.execute(post(baseUrl + "/case?email=" + emailAddress)
                .setHeader("Content-Length", "0")
                .build());

        assertThat(createCaseResponse.getStatusLine().getStatusCode(), is(HttpStatus.CREATED.value()));

        String responseBody = EntityUtils.toString(createCaseResponse.getEntity());
        JSONObject jsonObject = new JSONObject(responseBody);
        System.out.println("Case id " + jsonObject.getString("id"));
        return jsonObject.getString("id");
    }

    public JSONObject extendQuestionRoundDeadline(String hearingId) throws IOException {
        HttpResponse getQuestionResponse = client.execute(patch(baseUrl + "/continuous-online-hearings/" + hearingId)
                .build());

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getQuestionResponse.getEntity());

        return new JSONObject(responseBody);
    }

    public void recordTribunalViewResponse(String hearingId, String reply, String reason) throws IOException {
        HttpResponse response = client.execute(patch(baseUrl + "/continuous-online-hearings/" + hearingId + "/tribunal-view")
                .setEntity(new StringEntity("{\"reply\":\"" + reply + "\", \"reason\":\"" + reason + "\"}", APPLICATION_JSON))
                .build());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    public void uploadEvidence(String hearingId, String questionId) throws IOException {
        HttpEntity data = MultipartEntityBuilder.create()
                .setContentType(ContentType.MULTIPART_FORM_DATA)
                .addBinaryBody("file",
                        this.getClass().getClassLoader().getResourceAsStream("evidence.png"),
                        ContentType.IMAGE_PNG,
                        "evidence.png")
                .build();

        HttpResponse response = client.execute(post(baseUrl + "/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/evidence")
                .setEntity(data)
                .build());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
    }
}
