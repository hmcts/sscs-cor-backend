package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.client.methods.RequestBuilder.*;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

public class SscsCorBackendRequests {
    private final IdamTokens idamTokens;
    private String baseUrl;
    private CloseableHttpClient client;


    public SscsCorBackendRequests(IdamService idamService, String baseUrl, CloseableHttpClient client) {
        this.idamTokens = idamService.getIdamTokens();
        this.baseUrl = baseUrl;
        this.client = client;
    }

    public JSONObject getQuestion(String hearingId, String questionId) throws IOException {
        HttpResponse getQuestionResponse = getRequest("/continuous-online-hearings/" + hearingId + "/questions/" + questionId);

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getQuestionResponse.getEntity());

        return new JSONObject(responseBody);
    }

    public JSONObject getQuestions(String hearingId) throws IOException {
        HttpResponse getQuestionResponse = getRequest("/continuous-online-hearings/" + hearingId);

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getQuestionResponse.getEntity());

        return new JSONObject(responseBody);
    }

    public void answerQuestion(String hearingId, String questionId, String answer) throws IOException {
        HttpResponse getQuestionResponse = putRequest(
                "/continuous-online-hearings/" + hearingId + "/questions/" + questionId,
                new StringEntity("{\"answer\":\"" + answer + "\"}", APPLICATION_JSON)
        );

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    public void submitAnswer(String hearingId, String questionId) throws IOException {
        HttpResponse getQuestionResponse = postRequestNoBody("/continuous-online-hearings/" + hearingId + "/questions/" + questionId);

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    public JSONObject getOnlineHearing(String emailAddress) throws IOException {
        HttpResponse getOnlineHearingResponse = getRequest("/continuous-online-hearings?email=" + emailAddress);

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
        HttpResponse getQuestionResponse = patchRequestNoBody("/continuous-online-hearings/" + hearingId);

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getQuestionResponse.getEntity());

        return new JSONObject(responseBody);
    }

    public void recordTribunalViewResponse(String hearingId, String reply, String reason) throws IOException {
        HttpResponse response = patchRequest(
                "/continuous-online-hearings/" + hearingId + "/tribunal-view",
                new StringEntity("{\"reply\":\"" + reply + "\", \"reason\":\"" + reason + "\"}", APPLICATION_JSON)
        );
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

        HttpResponse response = postRequest("/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/evidence", data);
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

        HttpResponse response = client.execute(addHeaders(put(baseUrl + "/continuous-online-hearings/" + hearingId + "/evidence")
                .setEntity(data))
                .build());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
    }

    public JSONArray getDraftHearingEvidence(String hearingId) throws IOException {
        HttpResponse response = client.execute(addHeaders(get(baseUrl + "/continuous-online-hearings/" + hearingId + "/evidence"))
                .build());
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(response.getEntity());

        return new JSONArray(responseBody);
    }

    public void deleteEvidence(String hearingId, String questionId, String evidenceId) throws IOException {
        HttpResponse getQuestionResponse = deleteRequest("/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/evidence/" + evidenceId);

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    public void deleteHearingEvidence(String hearingId, String evidenceId) throws IOException {
        HttpResponse getQuestionResponse = deleteRequest("/continuous-online-hearings/" + hearingId + "/evidence/" + evidenceId);

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
                .setEntity(new StringEntity(
                        "{\"case_id\":\"" + caseId + "\", \"event_type\":\"" + event +
                                "\", \"online_hearing_id\":\"" + hearingId + "\"}",
                        APPLICATION_JSON
                ))
                .build());

        assertThat(resolveHearingResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
    }

    public void uploadAppellantStatement(String hearingId, String statement) throws IOException {
        String uri = "/continuous-online-hearings/" + hearingId + "/statement";
        HttpResponse getQuestionResponse = postRequest(uri, new StringEntity("{\"body\":\"" + statement + "\"}", APPLICATION_JSON));

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    private RequestBuilder addHeaders(RequestBuilder requestBuilder) {
        return requestBuilder
                .setHeader(HttpHeaders.AUTHORIZATION, idamTokens.getIdamOauth2Token())
                .setHeader("ServiceAuthorization", idamTokens.getServiceAuthorization());
    }

    private CloseableHttpResponse getRequest(String url) throws IOException {
        return client.execute(addHeaders(get(baseUrl + url))
                .build());
    }

    private CloseableHttpResponse putRequest(String url, StringEntity body) throws IOException {
        return client.execute(addHeaders(put(baseUrl + url))
                .setEntity(body)
                .build());
    }

    private CloseableHttpResponse postRequestNoBody(String url) throws IOException {
        return client.execute(addHeaders(post(baseUrl + url)
                .setHeader("Content-Length", "0"))
                .build());
    }

    private CloseableHttpResponse postRequest(String url, HttpEntity body) throws IOException {
        return client.execute(addHeaders(post(baseUrl + url))
                .setEntity(body)
                .build());
    }

    private CloseableHttpResponse patchRequestNoBody(String url) throws IOException {
        return client.execute(addHeaders(patch(baseUrl + url))
                .build());
    }

    private CloseableHttpResponse patchRequest(String url, StringEntity body) throws IOException {
        return client.execute(addHeaders(patch(baseUrl + url))
                .setEntity(body)
                .build());
    }

    private CloseableHttpResponse deleteRequest(String url) throws IOException {
        return client.execute(addHeaders(delete(baseUrl + url))
                .build());
    }
}
