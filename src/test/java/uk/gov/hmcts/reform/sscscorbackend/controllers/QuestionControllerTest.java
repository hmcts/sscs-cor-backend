package uk.gov.hmcts.reform.sscscorbackend.controllers;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;

public class QuestionControllerTest {
    QuestionController questionController;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    @Before
    public void setUp() {
        Question question = new Question();
        question.setQuestionHeaderText("What is the question?");

        questionController = new QuestionController();

    }
    private String json = "{\n" +
            "  \"current_question_state\": {\n" +
            "    \"state_datetime\": \"string\",\n" +
            "    \"state_name\": \"string\"\n" +
            "  },\n" +
            "  \"deadline_expiry_date\": \"string\",\n" +
            "  \"owner_reference\": \"string\",\n" +
            "  \"question_body_text\": \"string\",\n" +
            "  \"question_header_text\": \"string\",\n" +
            "  \"question_id\": \"string\",\n" +
            "  \"question_ordinal\": \"string\",\n" +
            "  \"question_round\": \"string\"\n" +
            "}";

    @Test
    public void getSampleQuestion() {

        stubFor(get(urlEqualTo("/continuous-online-hearings/1/questions/1"))
                .willReturn(aResponse().withBody(json)));

        ResponseEntity<Question> response = new QuestionController().getQuestion("1", "11");

        assertEquals("What is the question?", response.getBody().getQuestionHeaderText());
        assertEquals("1", response.getBody().getOnlineHearingId());
        assertEquals("11", response.getBody().getQuestionId());

    }
}
