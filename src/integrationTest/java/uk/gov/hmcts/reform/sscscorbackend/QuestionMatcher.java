package uk.gov.hmcts.reform.sscscorbackend;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.emptyList;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;

public class QuestionMatcher extends TypeSafeMatcher<String> {

    private final List<String> expected = new ArrayList<>();

    public QuestionMatcher(String headerText, String bodyText, String answerText, ZonedDateTime answerDate) {
        this(headerText, bodyText, answerText, answerDate, emptyList());
    }

    public QuestionMatcher(String headerText, String bodyText, String answerText, ZonedDateTime answerDate, List<Evidence> evidenceList) {
        expected.add("\"question_header_text\":\"" + headerText + "\"");
        expected.add("\"question_body_text\":\"" + bodyText + "\"");
        expected.add("\"answer\":\"" + answerText + "\"");
        expected.add("\"answer_date\":\"" + answerDate.format(ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")) + "\"");
        evidenceList.stream().forEach(evidence -> {
            expected.add("\"file_name\":\"" + evidence.getFileName() + "\"");
        });
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
