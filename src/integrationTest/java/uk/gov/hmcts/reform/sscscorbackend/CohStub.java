package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.draft;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohQuestionReference;

public class CohStub {

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
            "  \"question_ordinal\": \"string\",\n" +
            "  \"question_round\": \"string\"\n" +
            "}";

    private static final String getAnswersJson = "[\n" +
            "  {\n" +
            "    \"answer_id\": \"{answer_id}\",\n" +
            "    \"answer_text\": \"{answer_text}\",\n" +
            "    \"current_answer_state\": {\n" +
            "      \"state_datetime\": \"string\",\n" +
            "      \"state_name\": \"string\"\n" +
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
            "            \"question_references\": {question_references}\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    private static final String questionReferenceJson = "{\n" +
            "          \"answers\": [\n" +
            "            {\n" +
            "              \"answer_id\": \"string\",\n" +
            "              \"answer_text\": \"string\",\n" +
            "              \"current_answer_state\": {\n" +
            "                \"state_datetime\": \"string\",\n" +
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
            "          \"question_body_text\": \"string\",\n" +
            "          \"question_header_text\": \"{question_header}\",\n" +
            "          \"question_id\": \"{question_id}\",\n" +
            "          \"question_ordinal\": \"{question_ordinal}\",\n" +
            "          \"question_round\": \"1\",\n" +
            "          \"uri\": \"string\"\n" +
            "        }";

    private static final String postHearingJson = "{\n" +
            "\"online_hearing_id\": \"{onlineHearingId}\"\n" +
            "}";


    private final WireMockServer wireMock;

    public CohStub(String url) {
        wireMock = new WireMockServer(Integer.valueOf(url.split(":")[2]));
        wireMock.start();
    }

    public void printAllRequests() {
        if (System.getenv("PRINT_REQUESTS") != null) {
            wireMock.findAll(RequestPatternBuilder.allRequests()).forEach(request -> {
                System.out.println("*********************************************************");
                System.out.println(request);
                System.out.println("*********************************************************");
            });
        }
    }

    public void stubGetQuestion(String hearingId, String questionId, String questionHeader, String questionBody) {
        wireMock.stubFor(get(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions/" + questionId))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(buildGetQuestionBody(questionHeader, questionBody)))
        );
    }

    public void stubGetAnswer(String hearingId, String questionId, String answer) {
        stubGetAnswer(hearingId, questionId, answer, UUID.randomUUID().toString());
    }

    public void stubGetAnswer(String hearingId, String questionId, String answer, String answerId) {
        wireMock.stubFor(get(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/answers"))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(buildGetAnswerBody(answer, answerId)))
        );
    }

    private String buildGetQuestionBody(String questionHeader, String questionBody) {
        return getQuestionJson.replace("{question_header}", questionHeader)
                .replace("{question_body}", questionBody);
    }

    private String buildGetAnswerBody(String answer, CharSequence answerId) {
        return getAnswersJson.replace("{answer_text}", answer)
                .replace("{answer_id}", answerId);
    }

    public void shutdown() {
        wireMock.stop();
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
                        .replace("{question_id}", questionSummary.getQuestionId())
                        .replace("{answer_state}", questionSummary.getAnswers().get(0).getCurrentAnswerState().getStateName())
                        .replace("{deadline_expiry_date}", questionSummary.getDeadlineExpiryDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
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

}
