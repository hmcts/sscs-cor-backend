package uk.gov.hmcts.reform.sscscorbackend;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class QuestionMatcher extends TypeSafeMatcher<String> {

    private final List<String> expected = new ArrayList<>();

    public QuestionMatcher(String headerText, String bodyText) {
        expected.add("\"question_header_text\":\"" + headerText + "\"");
        expected.add("\"question_body_text\":\"" + bodyText + "\"");
    }

    @Override
    public void describeTo(Description description) {
        description.appendValueList("body to contain (", " && ", ")", expected);
    }

    @Override
    protected boolean matchesSafely(String responseBody) {
        return expected.stream().allMatch(responseBody::contains);
    }
}
