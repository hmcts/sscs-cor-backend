package uk.gov.hmcts.reform.sscscorbackend;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class QuestionHeaderMatcher extends TypeSafeMatcher<String> {

    private final String expected;

    public QuestionHeaderMatcher(String headerText) {
        this.expected = "\"question_header_text\":\"" + headerText + "\"";
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
