package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;

public class QuestionServiceTest {
    private CohClient cohClient;
    private CohQuestion cohQuestion;
    private String onlineHearingId;
    private String questionId;
    private CohAnswer cohAnswer;
    private QuestionService underTest;

    @Before
    public void setUp() {
        cohClient = mock(CohClient.class);
        cohQuestion = someCohQuestion();
        onlineHearingId = cohQuestion.getOnlineHearingId();
        questionId = cohQuestion.getQuestionId();
        cohAnswer = someCohAnswer();
        underTest = new QuestionService(cohClient);
    }

    @Test
    public void getsAListOfQuestionsWhenThereIsOnlyOneRoundOfQuestions() {
        CohQuestionRounds cohQuestionRounds = someCohQuestionRoundsWithSingleRoundOfQuestions();
        String questionHeaderText = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0)
                .getQuestionHeaderText();
        QuestionSummary questionSummary = new QuestionSummary(questionHeaderText);
        when(cohClient.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(questionSummary));
    }

    @Test
    public void getsAListOfQuestionsWhenThereIsMultipleRoundsOfQuestions() {
        CohQuestionRounds cohQuestionRounds = someCohQuestionRoundsMultipleRoundsOfQuestions();
        String questionHeaderText = cohQuestionRounds.getCohQuestionRound().get(1)
                .getQuestionReferences().get(0)
                .getQuestionHeaderText();
        QuestionSummary questionSummary = new QuestionSummary(questionHeaderText);
        when(cohClient.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(questionSummary));
    }

    @Test
    public void getsAListOfQuestionsInTheCorrectOrderWhenTheyAreReturnedInTheIncorrectOrder() {
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(new CohQuestionRound(
                asList(new CohQuestionReference(2, "second question"), new CohQuestionReference(1, "first question")))
        ));
        String firstQuestionTitle = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(1)
                .getQuestionHeaderText();
        String secondQuestionTitle = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0)
                .getQuestionHeaderText();
        when(cohClient.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(new QuestionSummary(firstQuestionTitle), new QuestionSummary(secondQuestionTitle)));
    }

    @Test
    public void getsAQuestionWithAnAnswer() {
        when(cohClient.getQuestion(onlineHearingId, questionId)).thenReturn(cohQuestion);
        when(cohClient.getAnswers(onlineHearingId, questionId)).thenReturn(singletonList(cohAnswer));

        Question question = underTest.getQuestion(onlineHearingId, questionId);

        assertThat(question, is(Question.from(cohQuestion, cohAnswer)));
    }

    @Test
    public void getsAQuestionWithoutAnAnswer() {
        when(cohClient.getQuestion(onlineHearingId, questionId)).thenReturn(cohQuestion);
        when(cohClient.getAnswers(onlineHearingId, questionId)).thenReturn(emptyList());

        Question question = underTest.getQuestion(onlineHearingId, questionId);

        assertThat(question, is(Question.from(cohQuestion)));
    }

    @Test
    public void doesNotFindQuestion() {
        when(cohClient.getQuestion(onlineHearingId, questionId)).thenReturn(null);

        Question question = underTest.getQuestion(onlineHearingId, questionId);

        assertThat(question, is(nullValue()));
        verify(cohClient, never()).getAnswers(onlineHearingId, questionId);
    }

    @Test
    public void updateAnswerWhenQuestionHasNotAlreadyBeenAnswered() {
        String newAnswer = "new answer";
        when(cohClient.getAnswers(onlineHearingId, questionId)).thenReturn(emptyList());
        underTest.updateAnswer(onlineHearingId, questionId, newAnswer);

        verify(cohClient).createAnswer(onlineHearingId, questionId, new CohUpdateAnswer("answer_drafted", newAnswer));
    }

    @Test
    public void updateAnswerWhenQuestionHasAlreadyBeenAnswered() {
        String newAnswer = "new answer";
        String answerId = "some-id";
        when(cohClient.getAnswers(onlineHearingId, questionId)).thenReturn(
                singletonList(new CohAnswer(answerId, "original answer"))
        );
        underTest.updateAnswer(onlineHearingId, questionId, newAnswer);

        verify(cohClient).updateAnswer(onlineHearingId, questionId, answerId, new CohUpdateAnswer("answer_drafted", newAnswer));
    }
}