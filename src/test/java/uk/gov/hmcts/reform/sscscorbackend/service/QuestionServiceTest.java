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
        CohQuestionReference cohQuestionReference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0);
        String id = cohQuestionReference.getQuestionId();
        String questionHeaderText = cohQuestionReference.getQuestionHeaderText();
        QuestionSummary questionSummary = new QuestionSummary(id, questionHeaderText);
        when(cohClient.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(questionSummary));
    }

    @Test
    public void getsAListOfQuestionsWhenThereIsMultipleRoundsOfQuestions() {
        CohQuestionRounds cohQuestionRounds = someCohQuestionRoundsMultipleRoundsOfQuestions();
        CohQuestionReference cohQuestionReference = cohQuestionRounds.getCohQuestionRound().get(1)
                .getQuestionReferences().get(0);
        String id = cohQuestionReference.getQuestionId();
        String questionHeaderText = cohQuestionReference.getQuestionHeaderText();
        QuestionSummary questionSummary = new QuestionSummary(id, questionHeaderText);
        when(cohClient.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(questionSummary));
    }

    @Test
    public void getsAListOfQuestionsInTheCorrectOrderWhenTheyAreReturnedInTheIncorrectOrder() {
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(new CohQuestionRound(
                asList(new CohQuestionReference("questionId2", 2, "second question"), new CohQuestionReference("questionId1", 1, "first question")))
        ));
        CohQuestionReference firstcohQuestionReference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(1);
        String firstQuestionId = firstcohQuestionReference.getQuestionId();
        String firstQuestionTitle = firstcohQuestionReference.getQuestionHeaderText();
        CohQuestionReference secondCohQuestionReference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0);
        String secondQuestionId = secondCohQuestionReference.getQuestionId();
        String secondQuestionTitle = secondCohQuestionReference.getQuestionHeaderText();
        when(cohClient.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(
                new QuestionSummary(firstQuestionId, firstQuestionTitle),
                new QuestionSummary(secondQuestionId, secondQuestionTitle)
                )
        );
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