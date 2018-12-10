package uk.gov.hmcts.reform.sscscorbackend.domain;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohQuestion;

import java.util.Optional;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;

public class CohQuestionTest {
    @Test
    public void getAnswer() {
        CohState answerSubmitted = DataFixtures.someCohState("answer_submitted");
        CohAnswer answer = new CohAnswer("answerId", "Some answer", answerSubmitted, singletonList(answerSubmitted));
        CohQuestion cohQuestion = someCohQuestion(answer);

        assertThat(cohQuestion.getAnswer(), is(Optional.of(answer)));
    }

    @Test
    public void getAnswerWhenNotAnswered() {
        CohQuestion cohQuestion = someCohQuestion();
        assertThat(cohQuestion.getAnswer(), is(Optional.empty()));
    }

    @Test
    public void getSubmittedDate() {
        CohState answerSubmitted = DataFixtures.someCohState("answer_submitted");
        CohAnswer answer = new CohAnswer("answerId", "Some answer", answerSubmitted, singletonList(answerSubmitted));
        CohQuestion cohQuestion = someCohQuestion(answer);

        assertThat(cohQuestion.getSubmittedDate(), is(Optional.of(answerSubmitted.getStateDateTime())));
    }

    @Test
    public void getSubmittedDateWhenAnswerDrafted() {
        CohState answerSubmitted = DataFixtures.someCohState("answer_drafted");
        CohAnswer answer = new CohAnswer("answerId", "Some answer", answerSubmitted, singletonList(answerSubmitted));
        CohQuestion cohQuestion = someCohQuestion(answer);

        assertThat(cohQuestion.getSubmittedDate(), is(Optional.empty()));
    }

    @Test
    public void getSubmittedDateWhenNoAnswer() {
        CohQuestion cohQuestion = someCohQuestion();

        assertThat(cohQuestion.getSubmittedDate(), is(Optional.empty()));
    }

    @Test
    public void getSubmittedDateWhenAnswerNull() {
        CohQuestion cohQuestion = new CohQuestion("someHearingId", 1, "someQuestionId", 1, "someHeader", "someBody", emptyList(), null);

        assertThat(cohQuestion.getSubmittedDate(), is(Optional.empty()));
    }

}