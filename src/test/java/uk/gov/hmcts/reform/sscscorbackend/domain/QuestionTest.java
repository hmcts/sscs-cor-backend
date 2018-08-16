package uk.gov.hmcts.reform.sscscorbackend.domain;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohAnswer;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohQuestion;

import org.junit.Test;

public class QuestionTest {
    @Test
    public void canConvertCohQuestionToQuestion() {
        CohQuestion cohQuestion = someCohQuestion();
        Question question = Question.from(cohQuestion);

        assertThat(question.getOnlineHearingId(), is(cohQuestion.getOnlineHearingId()));
        assertThat(question.getQuestionHeaderText(), is(cohQuestion.getQuestionHeaderText()));
        assertThat(question.getQuestionBodyText(), is(cohQuestion.getQuestionBodyText()));

        assertThat(question.getAnswer(), is(nullValue()));
    }

    @Test
    public void canConvertCohQuestionAndCohAnswerToQuestion() {
        CohQuestion cohQuestion = someCohQuestion();
        CohAnswer cohAnswer = someCohAnswer();
        Question question = Question.from(cohQuestion, cohAnswer);

        assertThat(question.getOnlineHearingId(), is(cohQuestion.getOnlineHearingId()));
        assertThat(question.getQuestionHeaderText(), is(cohQuestion.getQuestionHeaderText()));
        assertThat(question.getQuestionBodyText(), is(cohQuestion.getQuestionBodyText()));

        assertThat(question.getAnswer(), is(cohAnswer.getAnswerText()));
    }
}