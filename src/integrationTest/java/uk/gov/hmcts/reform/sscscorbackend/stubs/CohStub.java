package uk.gov.hmcts.reform.sscscorbackend.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.valueOf;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.draft;

import com.github.tomakehurst.wiremock.matching.RegexPattern;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohQuestionReference;

public class CohStub extends BaseStub {

    private static final String getQuestionJson = "{\n" +
            "  \"current_question_state\": {\n" +
            "    \"state_datetime\": \"string\",\n" +
            "    \"state_name\": \"string\"\n" +
            "  },\n" +
            "  \"deadline_expiry_date\": \"string\",\n" +
            "  \"owner_reference\": \"string\",\n" +
            "  \"question_body_text\": \"{question_body}\",\n" +
            "  \"question_header_text\": \"{question_header}\",\n" +
            "  \"question_id\": \"string\",\n" +
            "  \"question_ordinal\": \"1\",\n" +
            "  \"question_round\": \"1\"\n" +
            "}";

    private static final String getQuestionWithAnswerJson = "{\n" +
            "  \"current_question_state\": {\n" +
            "    \"state_datetime\": \"string\",\n" +
            "    \"state_name\": \"string\"\n" +
            "  },\n" +
            "  \"deadline_expiry_date\": \"string\",\n" +
            "  \"owner_reference\": \"string\",\n" +
            "  \"question_body_text\": \"{question_body}\",\n" +
            "  \"question_header_text\": \"{question_header}\",\n" +
            "  \"question_id\": \"{question_id}\",\n" +
            "  \"question_ordinal\": \"1\",\n" +
            "  \"question_round\": \"1\"," +
            "  \"answers\": {answers}\n" +
            "}";


    private static final String getAnswersJson = "[\n" +
            "  {\n" +
            "    \"answer_id\": \"{answer_id}\",\n" +
            "    \"answer_text\": \"{answer_text}\",\n" +
            "    \"current_answer_state\": {\n" +
            "      \"state_datetime\": \"{answer_datetime}\",\n" +
            "      \"state_name\": \"{answer_state}\"\n" +
            "    }\n" +
            "  }\n" +
            "]";

    private static final String getQuestionRoundsJson = "{\n" +
            "    \"previous_question_round\": 1,\n" +
            "    \"current_question_round\": 1,\n" +
            "    \"next_question_round\": 2,\n" +
            "    \"max_number_of_question_rounds\": 0,\n" +
            "    \"question_rounds\": [\n" +
            "        {\n" +
            "            \"question_round_number\": \"1\",\n" +
            "            \"question_references\": {question_references},\n" +
            "            \"question_round_state\": { \"state_name\": \"question_issued\" }\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    private static final String questionReferenceJson = "{\n" +
            "          \"answers\": [\n" +
            "            {\n" +
            "              \"answer_id\": \"string\",\n" +
            "              \"answer_text\": \"string\",\n" +
            "              \"current_answer_state\": {\n" +
            "                \"state_datetime\": \"2019-05-28T15:19:40Z\",\n" +
            "                \"state_desc\": \"string\",\n" +
            "                \"state_name\": \"{answer_state}\"\n" +
            "              },\n" +
            "              \"history\": [\n" +
            "                {\n" +
            "                  \"state_datetime\": \"string\",\n" +
            "                  \"state_desc\": \"string\",\n" +
            "                  \"state_name\": \"string\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"uri\": \"string\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"current_question_state\": {\n" +
            "            \"state_datetime\": \"string\",\n" +
            "            \"state_desc\": \"string\",\n" +
            "            \"state_name\": \"question_issued\"\n" +
            "          },\n" +
            "          \"deadline_expiry_date\": \"{deadline_expiry_date}\",\n" +
            "          \"history\": [\n" +
            "            {\n" +
            "              \"state_datetime\": \"string\",\n" +
            "              \"state_desc\": \"string\",\n" +
            "              \"state_name\": \"string\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"owner_reference\": \"string\",\n" +
            "          \"question_body_text\": \"{question_body}\",\n" +
            "          \"question_header_text\": \"{question_header}\",\n" +
            "          \"question_id\": \"{question_id}\",\n" +
            "          \"question_ordinal\": \"{question_ordinal}\",\n" +
            "          \"question_round\": \"1\",\n" +
            "          \"uri\": \"string\"\n" +
            "        }";

    private static final String onlineHearingJson = "{\n" +
            "    \"online_hearing_id\": \"{online_hearing_id}\",\n" +
            "    \"case_id\": \"{case_id}\",\n" +
            "    \"start_date\": \"2018-08-15T12:57:07Z\",\n" +
            "    \"panel\": [\n" +
            "        {\n" +
            "            \"name\": \"John Dead\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"current_state\": {\n" +
            "        \"state_name\": \"continuous_online_hearing_started\",\n" +
            "        \"state_desc\": \"Continuous Online Hearing Started\",\n" +
            "        \"state_datetime\": \"2018-08-20T16:17:06Z\"\n" +
            "    },\n" +
            "    \"history\": [\n" +
            "        {\n" +
            "            \"state_name\": \"continuous_online_hearing_started\",\n" +
            "            \"state_desc\": \"Continuous Online Hearing Started\",\n" +
            "            \"state_datetime\": \"2018-08-20T16:17:06Z\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    private static final String onlineHearingsJson = "{\n" +
            "    \"online_hearings\": [\n" +
            onlineHearingJson +
            "    ]\n" +
            "}";

    private static final String postHearingJson = "{\n" +
            "\"online_hearing_id\": \"{onlineHearingId}\"\n" +
            "}";


    public CohStub(String url) {
        super(url);
    }

    public void stubGetQuestion(String hearingId, String questionId, String questionHeader, String questionBody) {
        wireMock.stubFor(get(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions/" + questionId))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(buildGetQuestionBody(questionHeader, questionBody)))
        );
    }

    public void stubGetQuestionWithAnswer(String hearingId, String questionId, String questionHeader, String questionBody, String answer, String answerId, String answerState, ZonedDateTime answerDate) {
        wireMock.stubFor(get(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions/" + questionId))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(buildGetQuestionWithAnswerBody(questionId, questionHeader, questionBody, answer, answerId, answerState, answerDate)))
        );
    }

    public void stubGetAnswer(String hearingId, String questionId, String answer) {
        stubGetAnswer(hearingId, questionId, answer, UUID.randomUUID().toString(), "draft", ZonedDateTime.now());
    }

    public void stubGetAnswer(String hearingId, String questionId, String answer, String answerId, String answerState, ZonedDateTime answerDate) {
        wireMock.stubFor(get(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/answers"))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(buildGetAnswerBody(answer, answerId, answerState, answerDate)))
        );
    }

    private String buildGetQuestionBody(String questionHeader, String questionBody) {
        return getQuestionJson.replace("{question_header}", questionHeader)
                .replace("{question_body}", questionBody);
    }

    private String buildGetQuestionWithAnswerBody(String questionId, String questionHeader, String questionBody, String answer, String answerId, String answerState, ZonedDateTime answerDate) {
        String answerJson = buildGetAnswerBody(answer, answerId, answerState, answerDate);
        return getQuestionWithAnswerJson
                .replace("{question_id}", questionId)
                .replace("{question_header}", questionHeader)
                .replace("{question_body}", questionBody)
                .replace("{answers}", answerJson);
    }

    private String buildGetAnswerBody(String answer, String answerId, String answerState, ZonedDateTime answerDate) {
        return getAnswersJson.replace("{answer_text}", answer)
                .replace("{answer_id}", answerId)
                .replace("{answer_state}", answerState)
                .replace("{answer_datetime}", answerDate.format(ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")));
    }

    public void stubCannotFindQuestion(String hearingId, String questionId) {
        wireMock.stubFor(get(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions/" + questionId))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(notFound()));
    }

    public void stubCannotFindAnswers(String hearingId, String questionId) {
        wireMock.stubFor(get(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/answers"))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson("[]")));
    }

    public void stubCreateAnswer(String hearingId, String questionId, String newAnswer) {
        wireMock.stubFor(post("/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/answers")
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .withRequestBody(equalToJson("{\"answer_state\":\"answer_drafted\", \"answer_text\":\"" + newAnswer + "\"}"))
                .willReturn(created())
        );
    }

    public void stubUpdateAnswer(String hearingId, String questionId, String newAnswer, String answerId) {
        stubUpdateAnswer(hearingId, questionId, newAnswer, answerId, draft);
    }

    public void stubUpdateAnswer(String hearingId, String questionId, String newAnswer, String answerId, AnswerState answerState) {
        wireMock.stubFor(put("/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/answers/" + answerId)
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .withRequestBody(equalToJson("{\"answer_state\":\"" + answerState.getCohAnswerState() + "\", \"answer_text\":\"" + newAnswer + "\"}"))
                .willReturn(created())
        );
    }

    public void stubGetAllQuestionRounds(String hearingId, CohQuestionReference... questionReferences) {
        String body = buildGetAllQuestionsRoundsBody(questionReferences);
        wireMock.stubFor(get("/continuous-online-hearings/" + hearingId + "/questionrounds")
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(body)));
    }

    private String buildGetAllQuestionsRoundsBody(CohQuestionReference... startQuestionReferences) {
        final AtomicInteger index = new AtomicInteger(1);
        String questionReferences = Arrays.stream(startQuestionReferences)
                .map(questionSummary -> questionReferenceJson
                        .replace("{question_ordinal}", "" + index.getAndIncrement())
                        .replace("{question_header}", questionSummary.getQuestionHeaderText())
                        .replace("{question_body}", questionSummary.getQuestionBodyText())
                        .replace("{question_id}", questionSummary.getQuestionId())
                        .replace("{answer_state}", questionSummary.getAnswers().get(0).getCurrentAnswerState().getStateName())
                        .replace("{deadline_expiry_date}", questionSummary.getDeadlineExpiryDate())
                )
                .collect(joining(", ", "[", "]"));

        return getQuestionRoundsJson.replace("{question_references}", questionReferences);
    }

    private String getAnswerState(AnswerState answerState) {
        if (answerState.equals(AnswerState.unanswered)) {
            throw new IllegalArgumentException("Setup cannot handle unanswered questions");
        }
        return answerState.getCohAnswerState();
    }

    private String getDecisionsBody(String hearingId, long caseId) {
        InputStream decisionStream = getClass().getClassLoader().getResourceAsStream("json/get_decision.json");
        String decisionAsString = new BufferedReader(new InputStreamReader(decisionStream)).lines().collect(joining("\n"));
        return decisionAsString.replace("{online_hearing_id}", hearingId)
                .replace("{case_reference}", caseId + "");
    }

    private String getDecisionRepliesBody(String reply) {
        InputStream decisionRepliesStream = getClass().getClassLoader().getResourceAsStream("json/get_decision_replies.json");
        String decisionRepliesAsString = new BufferedReader(new InputStreamReader(decisionRepliesStream)).lines().collect(joining("\n"));
        return decisionRepliesAsString.replace("{decision_reply}", reply);
    }

    public void stubPostOnlineHearing(String onlineHearingId) {
        wireMock.stubFor(post(urlEqualTo("/continuous-online-hearings"))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(postHearingJson.replace("{onlineHearingId}", onlineHearingId))));
    }

    //TODO need to handle this exception
    public void stubDuplicateCase() {
        wireMock.stubFor(post(urlEqualTo("/continuous-online-hearings"))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(status(409)));
    }

    public void stubGetOnlineHearing(Long caseId, String onlineHearingId) {
        String body = onlineHearingsJson.replace("{online_hearing_id}", onlineHearingId)
                .replace("{case_id}", valueOf(caseId));
        wireMock.stubFor(get("/continuous-online-hearings?case_id=" + caseId)
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(body)));

        wireMock.stubFor(get("/continuous-online-hearings/" + onlineHearingId)
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(onlineHearingJson.replace("{online_hearing_id}", onlineHearingId)
                        .replace("{case_id}", valueOf(caseId))
                )));
    }

    public void stubExtendQuestionRoundDeadline(String hearingId) {
        wireMock.stubFor(put(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions-deadline-extension"))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(ok("{}")));
        delayForWiremockToWork();
    }

    public void stubCannotExtendQuestionRoundDeadline(String hearingId) {
        wireMock.stubFor(put(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions-deadline-extension"))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(status(424)));
        delayForWiremockToWork();
    }

    // This is a hack due to this issue https://github.com/tomakehurst/wiremock/issues/97. Tests were failing due to
    // a ConnectionResetException. Maybe we can rework how we start the stubs to fx this.
    private void delayForWiremockToWork() {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stubGetDecisions(String hearingId, long caseId) {
        wireMock.stubFor(get("/continuous-online-hearings/" + hearingId + "/decisions")
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(getDecisionsBody(hearingId, caseId))));
    }

    public void stubGetDecisionNotFound(String hearingId) {
        wireMock.stubFor(get("/continuous-online-hearings/" + hearingId + "/decisions")
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(notFound()
                        .withBody("Unable to find decision")
                        .withHeader("Content-Type", "text/plain;charset=UTF-8")
                )
        );
    }

    public void stubPostDecisionReply(String hearingId, String reply, String reason) {
        // COH won't accept an empty reason, therefore it is set to the reply if it's empty
        if (reason.isEmpty()) {
            reason = reply;
        }
        wireMock.stubFor(post("/continuous-online-hearings/" + hearingId + "/decisionreplies")
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .withRequestBody(equalToJson("{\"decision_reply\":\"" + reply + "\", \"decision_reply_reason\":\"" + reason + "\"}"))
                .willReturn(status(201)
                        .withBody("{\n" +
                                "  \"decision_reply_id\": \"123\"\n" +
                                "}")
                )
        );
    }

    public void stubGetDecisionReplies(String hearingId, String reply) {
        wireMock.stubFor(get("/continuous-online-hearings/" + hearingId + "/decisionreplies")
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(getDecisionRepliesBody(reply))));
    }

    public void stubGetDecisionRepliesEmpty(String hearingId) {
        wireMock.stubFor(get("/continuous-online-hearings/" + hearingId + "/decisionreplies")
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson("{\n" +
                        "  \"decision_replies\": []\n" +
                        "}")));
    }

    public void stubGetDecisionRepliesNotFound(String hearingId) {
        wireMock.stubFor(get("/continuous-online-hearings/" + hearingId + "/decisionreplies")
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(notFound()
                        .withBody("Unable to find decision")
                        .withHeader("Content-Type", "text/plain;charset=UTF-8")
                )
        );
    }

    public void stubGetConversation(String hearingId) {
        wireMock.stubFor(get("/continuous-online-hearings/" + hearingId + "/conversations")
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson("{\n" +
                        "    \"online_hearing\": {\n" +
                        "        \"questions\": [\n" +
                        "            {\n" +
                        "                \"question_round\": \"1\",\n" +
                        "                \"question_ordinal\": \"1\",\n" +
                        "                \"question_header_text\": \"header text\",\n" +
                        "                \"question_body_text\": \"body text\",\n" +
                        "                \"question_id\": \"512d223c-5d00-4521-bea8-74b734136cc5\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"relisting\": {\n" +
                        "            \"reason\": \"some relisting reason\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"))
        );
    }
}
