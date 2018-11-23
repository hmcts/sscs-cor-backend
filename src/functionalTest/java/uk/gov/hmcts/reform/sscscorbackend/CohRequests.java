package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.client.methods.RequestBuilder.*;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.function.Supplier;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

public class CohRequests {
    private final IdamTokens idamTokens;
    private String cohBaseUrl;
    private HttpClient cohClient;

    public CohRequests(IdamService idamService, String cohBaseUrl, HttpClient cohClient) {
        idamTokens = idamService.getIdamTokens();
        this.cohBaseUrl = cohBaseUrl;
        this.cohClient = cohClient;
    }

    public String createHearing() throws IOException {
        return createHearing(new CcdIdGenerator().generateUid());
    }

    public String createHearing(String caseId) throws IOException {
        String hearingId = makePostRequest(cohClient, cohBaseUrl + "/continuous-online-hearings", "{\n" +
                "  \"case_id\": \"" + caseId + "\",\n" +
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

    public String createQuestion(String hearingId) throws IOException {
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

    public void issueQuestionRound(String hearingId) throws IOException, InterruptedException {
        makePutRequest(cohClient, cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/questionrounds/1",
                "{\"state_name\": \"question_issue_pending\"}"
        );

        waitUntil(roundIssued(hearingId), 60L, "Question round has not been issued in 60 seconds.");
    }

    public String createAnswer(String hearingId, String questionId, String answerText) throws IOException {
        String url = cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/answers";
        String answerId = makePostRequest(cohClient, url, "{\n" +
                "  \"answer_state\": \"answer_submitted\",\n" +
                "  \"answer_text\": \"" + answerText + "\"\n" +
                "}", "answer_id");
        System.out.println("Answer id " + answerId);
        return answerId;
    }

    public String createDecision(String hearingId, String decisionAward, String decisionHeader,
                                 String decisionReason, String decisionText) throws IOException {
        String url = cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/decisions";
        String decisionId = makePostRequest(cohClient, url, "{\n" +
                "  \"decision_award\": \"" + decisionAward + "\",\n" +
                "  \"decision_header\": \"" + decisionHeader + "\",\n" +
                "  \"decision_reason\": \"" + decisionReason + "\",\n" +
                "  \"decision_text\": \"" + decisionText + "\"\n" +
                "}", "decision_id");
        System.out.println("Decision id " + decisionId);
        return decisionId;
    }

    public void issueDecision(String hearingId, String decisionAward, String decisionHeader,
                              String decisionReason, String decisionText) throws IOException, InterruptedException {
        String url = cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/decisions";
        makePutRequest(cohClient, url,
                "{\n" +
                        "  \"decision_award\": \"" + decisionAward + "\",\n" +
                        "  \"decision_header\": \"" + decisionHeader + "\",\n" +
                        "  \"decision_reason\": \"" + decisionReason + "\",\n" +
                        "  \"decision_text\": \"" + decisionText + "\",\n" +
                        "  \"decision_state\": \"decision_issue_pending\"\n" +
                        "}"
        );

        waitUntil(decisionIssued(hearingId), 60L, "Decision has not been issued in 60 seconds.");
    }

    public int getDeadlineExtensionCount(String hearingId) throws IOException {
        String url = cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/questionrounds";
        int deadlineExtensionCount = makeGetRequest(cohClient, url, null).getJSONArray("question_rounds")
                .getJSONObject(0).getInt("deadline_extension_count");
        return deadlineExtensionCount;
    }

    public JSONObject getDecisionReplies(String hearingId) throws IOException {
        String url = cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/decisionreplies";
        return makeGetRequest(cohClient, url, null);
    }

    public String addDecisionReply(String hearingId, String reply, String reason) throws IOException {
        String url = cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/decisionreplies";
        String decisionReplyId = makePostRequest(cohClient, url, "{\n" +
                "  \"decision_reply\": \"" + reply + "\",\n" +
                "  \"decision_reply_reason\": \"" + reason + "\"\n" +
                "}", "decision_reply_id");
        return decisionReplyId;
    }

    private Supplier<Boolean> roundIssued(String hearingId) {
        return () -> {
            try {
                String roundState = makeGetRequest(
                        cohClient,
                        cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/questionrounds/1",
                        "question_round_state.state_name"
                ).getJSONObject("question_round_state").getString("state_name");
                return roundState.equals("question_issued");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Supplier<Boolean> decisionIssued(String hearingId) {
        return () -> {
            try {
                String decisionState = makeGetRequest(
                        cohClient,
                        cohBaseUrl + "/continuous-online-hearings/" + hearingId + "/decisions",
                        "decision_state.state_name"
                ).getJSONObject("decision_state").getString("state_name");
                return decisionState.equals("decision_issued");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static void waitUntil(Supplier<Boolean> condition, long timeoutInSeconds, String timeoutMessage) throws InterruptedException {
        long timeout = timeoutInSeconds * 1000L * 1000000L;
        long startTime = System.nanoTime();
        while (true) {
            if (condition.get()) {
                break;
            } else if (System.nanoTime() - startTime >= timeout) {
                throw new RuntimeException(timeoutMessage);
            }
            Thread.sleep(100L);
        }
    }

    private String makePostRequest(HttpClient client, String uri, String body, String responseValue) throws IOException {
        HttpResponse httpResponse = client.execute(post(uri)
                .setHeader(HttpHeaders.AUTHORIZATION, idamTokens.getIdamOauth2Token())
                .setHeader("ServiceAuthorization", idamTokens.getServiceAuthorization())
                .setEntity(new StringEntity(body, APPLICATION_JSON))
                .build());

        assertThat(httpResponse.getStatusLine().getStatusCode(), is(HttpStatus.CREATED.value()));
        String responseBody = EntityUtils.toString(httpResponse.getEntity());

        return new JSONObject(responseBody).getString(responseValue);
    }

    private void makePutRequest(HttpClient client, String uri, String body) throws IOException {
        HttpResponse httpResponse = client.execute(put(uri)
                .setHeader(HttpHeaders.AUTHORIZATION, idamTokens.getIdamOauth2Token())
                .setHeader("ServiceAuthorization", idamTokens.getServiceAuthorization())
                .setEntity(new StringEntity(body, APPLICATION_JSON))
                .build());

        assertThat(httpResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
    }

    private JSONObject makeGetRequest(HttpClient client, String uri, String responseValue) throws IOException {
        HttpResponse httpResponse = client.execute(get(uri)
                .setHeader(HttpHeaders.AUTHORIZATION, idamTokens.getIdamOauth2Token())
                .setHeader("ServiceAuthorization", idamTokens.getServiceAuthorization())
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build());

        assertThat(httpResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
        String responseBody = EntityUtils.toString(httpResponse.getEntity());
        return new JSONObject(responseBody);
    }


}
