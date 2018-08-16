package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.util.UUID;

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
        wireMock.stubFor(put("/continuous-online-hearings/" + hearingId + "/questions/" + questionId + "/answers/" + answerId)
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .withRequestBody(equalToJson("{\"answer_state\":\"answer_drafted\", \"answer_text\":\"" + newAnswer + "\"}"))
                .willReturn(created())
        );
    }
}
