package uk.gov.hmcts.reform.sscscorbackend.smoke;

import io.restassured.RestAssured;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class StacktraceTest {
    private static final String BODY_WITHOUT_STACK_TRACE = "An error has occurred";
    @Rule
    public Retry retry = new Retry(3);

    private final String baseUrl = System.getenv("TEST_URL");

    @Test
    public void testStackTracesNotShownInProd() {
        RestAssured.baseURI = baseUrl;
        boolean shouldNotContainStacktrace = baseUrl.contains("prod");

        RestAssured.useRelaxedHTTPSValidation();
        RestAssured
                .given()
                .when()
                .get("/continuous-online-hearings/does-not-exists")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new TypeSafeMatcher<String>() {
                    @Override
                    protected boolean matchesSafely(String body) {
                        return body.equals(BODY_WITHOUT_STACK_TRACE) == shouldNotContainStacktrace;
                    }

                    @Override
                    public void describeTo(Description description) {
                        String contains = shouldNotContainStacktrace ? "to" : "not to";
                        description.appendText("body " + contains + " Exception stack trace.");
                    }

                    @Override
                    protected void describeMismatchSafely(String body, Description mismatchDescription) {
                        String contains = shouldNotContainStacktrace ? "did not" : "did";
                        mismatchDescription.appendText(" it " + contains + "\n").appendValue(body);
                    }
                });
    }
}
