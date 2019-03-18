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
import org.json.JSONArray;
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

    public void uploadEvidence(String hearingId, String questionId, String fileName) throws IOException {
        HttpEntity data = MultipartEntityBuilder.create()
                .setContentType(ContentType.MULTIPART_FORM_DATA)
                .addBinaryBody("file",
                        this.getClass().getClassLoader().getResourceAsStream(fileName),
                        ContentType.IMAGE_PNG,
                        fileName)
                .build();

        HttpResponse response = client.execute(post(baseUrl + "/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/evidence")
                .setEntity(data)
                .build());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
    }

    public void uploadHearingEvidence(String hearingId, String fileName) throws IOException {
        HttpEntity data = MultipartEntityBuilder.create()
                .setContentType(ContentType.MULTIPART_FORM_DATA)
                .addBinaryBody("file",
                        this.getClass().getClassLoader().getResourceAsStream(fileName),
                        ContentType.IMAGE_PNG,
                        fileName)
                .build();

        HttpResponse response = client.execute(put(baseUrl + "/continuous-online-hearings/" + hearingId + "/evidence")
                .setEntity(data)
                .build());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
    }

    public JSONArray getDraftHearingEvidence(String hearingId) throws IOException {
        HttpResponse response = client.execute(get(baseUrl + "/continuous-online-hearings/" + hearingId + "/evidence")
                .build());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(response.getEntity());

        return new JSONArray(responseBody);
    }

    public void deleteEvidence(String hearingId, String questionId, String evidenceId) throws IOException {
        HttpResponse getQuestionResponse = client.execute(delete(
                baseUrl + "/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/evidence/" + evidenceId
        ).build());

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    public void deleteHearingEvidence(String hearingId, String evidenceId) throws IOException {
        HttpResponse getQuestionResponse = client.execute(delete(
                baseUrl + "/continuous-online-hearings/" + hearingId + "/evidence/" + evidenceId
        ).build());

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    public void cohHearingRelisted(String hearingId, String caseId) throws IOException {
        cohEvent(hearingId, caseId, "continuous_online_hearing_relisted");
    }

    public void cohDecisionIssued(String hearingId, String caseId) throws IOException {
        cohEvent(hearingId, caseId, "decision_issued");
    }

    private void cohEvent(String hearingId, String caseId, String event) throws IOException {
        HttpResponse resolveHearingResponse = client.execute(post(baseUrl + "/notify/onlinehearing")
                .setEntity(new StringEntity("{\"case_id\":\"" + caseId + "\", \"event_type\":\"" + event +
                        "\", \"online_hearing_id\":\"" + hearingId + "\"}", APPLICATION_JSON))
                .build());

        assertThat(resolveHearingResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
    }
}
