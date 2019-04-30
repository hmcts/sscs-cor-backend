package uk.gov.hmcts.reform.sscscorbackend.service.pdf.util;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionSummary;

public class QuestionUtilsTest {
    @Test
    public void doesNotCleanNoneHtml() {
        String plainTextQuestion = "plain text body";
        List<QuestionSummary> originalQuestions = singletonList(
                new QuestionSummary("id", 1, "header", plainTextQuestion, AnswerState.submitted, "some answer")
        );
        List<QuestionSummary> cleanedQuestions = new QuestionUtils().cleanUpQuestionsHtml(originalQuestions);

        assertThat(cleanedQuestions, is(originalQuestions));
    }

    @Test
    public void cleansNoneXhtml() {
        String htmlQuestion = "<p><br></p>";
        List<QuestionSummary> originalQuestions = singletonList(
                new QuestionSummary("id", 1, "header", htmlQuestion, AnswerState.submitted, "some answer")
        );
        List<QuestionSummary> cleanedQuestions = new QuestionUtils().cleanUpQuestionsHtml(originalQuestions);

        assertThat(cleanedQuestions.size(), is(1));
        assertThat(cleanedQuestions.get(0).getQuestionBodyText(), is("<p><br /></p>"));
    }

    @Test
    public void doesNotChangeValidXhtml() {
        String htmlQuestion = "<p>This is valid content</p>";
        List<QuestionSummary> originalQuestions = singletonList(
                new QuestionSummary("id", 1, "header", htmlQuestion, AnswerState.submitted, "some answer")
        );
        List<QuestionSummary> cleanedQuestions = new QuestionUtils().cleanUpQuestionsHtml(originalQuestions);

        assertThat(cleanedQuestions.size(), is(1));
        assertThat(cleanedQuestions.get(0).getQuestionBodyText(), is(htmlQuestion));
    }

    @Test
    public void canHandleNbsp() {
        String htmlQuestion = "<p>This is valid content&nbsp;</p>";
        List<QuestionSummary> originalQuestions = singletonList(
                new QuestionSummary("id", 1, "header", htmlQuestion, AnswerState.submitted, "some answer")
        );
        List<QuestionSummary> cleanedQuestions = new QuestionUtils().cleanUpQuestionsHtml(originalQuestions);

        assertThat(cleanedQuestions.size(), is(1));
        assertThat(cleanedQuestions.get(0).getQuestionBodyText(), is(htmlQuestion));
    }

    @Test
    public void canHandleMultipleTags() {
        String htmlQuestion = "<p>This is valid content&nbsp;</p><p><br></p>";
        List<QuestionSummary> originalQuestions = singletonList(
                new QuestionSummary("id", 1, "header", htmlQuestion, AnswerState.submitted, "some answer")
        );
        List<QuestionSummary> cleanedQuestions = new QuestionUtils().cleanUpQuestionsHtml(originalQuestions);

        assertThat(cleanedQuestions.size(), is(1));
        assertThat(cleanedQuestions.get(0).getQuestionBodyText(), is("<p>This is valid content&nbsp;</p>\n<p><br /></p>"));
    }
}