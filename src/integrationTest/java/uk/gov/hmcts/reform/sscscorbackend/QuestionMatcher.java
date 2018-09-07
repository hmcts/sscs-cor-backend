package uk.gov.hmcts.reform.sscscorbackend;

import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class QuestionMatcher extends TypeSafeMatcher<String> {

    private final List<String> expected = new ArrayList<>();

    public QuestionMatcher(String headerText, String bodyText, String answerText, ZonedDateTime answerDate) {
        expected.add("\"question_header_text\":\"" + headerText + "\"");
        expected.add("\"question_body_text\":\"" + bodyText + "\"");
        expected.add("\"answer\":\"" + answerText + "\"");
        expected.add("\"answer_date\":\"" + answerDate.format(ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")) + "\"");
    }

    public QuestionMatcher(String headerText, String bodyText, String answerText, String answerStatus) {
        expected.add("\"question_header_text\":\"" + headerText + "\"");
        expected.add("\"question_body_text\":\"" + bodyText + "\"");
        expected.add("\"answer\":\"" + answerText + "\"");
        expected.add("\"answer_state\":\"" + answerStatus + "\"");
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
