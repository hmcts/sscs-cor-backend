package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RegexPattern;

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

    private final WireMockServer wireMock;

    public CohStub(String url) {
        wireMock = new WireMockServer(Integer.valueOf(url.split(":")[2]));
        wireMock.start();
    }

    public void stubGetQuestion(String hearingId, String questionId, String questionHeader, String questionBody) {
        wireMock.stubFor(get(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions/" + questionId))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(buildGetQuestionBody(questionHeader, questionBody)))
        );
    }

    private String buildGetQuestionBody(String questionHeader, String questionBody) {
        return getQuestionJson.replace("{question_header}", questionHeader)
                .replace("{question_body}", questionBody);
    }

    public void shutdown() {
        wireMock.stop();
    }

    public void stubCannotFindQuestion(String hearingId, String questionId) {
        wireMock.stubFor(get(urlEqualTo("/continuous-online-hearings/" + hearingId + "/questions/" + questionId))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(notFound()));
    }
}
