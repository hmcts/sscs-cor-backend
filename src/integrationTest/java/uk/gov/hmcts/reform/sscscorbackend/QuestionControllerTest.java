package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.RestAssured;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class QuestionControllerTest {

    private static final String QUESTION_HEADER = "Question Header";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    private String json = "{\n" +
            "  \"current_question_state\": {\n" +
            "    \"state_datetime\": \"string\",\n" +
            "    \"state_name\": \"string\"\n" +
            "  },\n" +
            "  \"deadline_expiry_date\": \"string\",\n" +
            "  \"owner_reference\": \"string\",\n" +
            "  \"question_body_text\": \"Question body\",\n" +
            "  \"question_header_text\": \"" + QUESTION_HEADER + "\",\n" +
            "  \"question_id\": \"string\",\n" +
            "  \"question_ordinal\": \"string\",\n" +
            "  \"question_round\": \"string\"\n" +
            "}";

    @Test
    public void getSampleQuestion() {
        stubFor(get(urlEqualTo("/continuous-online-hearings/1/questions/1"))
                .willReturn(okJson(json))
        );

        RestAssured.baseURI = "http://localhost:8090";
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured
                .given()
                .when()
                .get("/continuous-online-hearings/1/questions/1")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(new QuestionHeaderMatcher(QUESTION_HEADER));


    }

    private static class QuestionHeaderMatcher extends TypeSafeMatcher<String> {

        private final String expected;

        public QuestionHeaderMatcher(String headerText) {
            this.expected = "\"question_header_text\": \"" + headerText + "\"";
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("body to contain ").appendValue(expected);
        }

        @Override
        protected boolean matchesSafely(String responseBody) {
            return responseBody.contains(expected);
        }
    }
}
