package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.*;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;

public class QuestionServiceTest {
    private CohService cohService;
    private CohQuestion cohQuestion;
    private String onlineHearingId;
    private String questionId;
    private CohAnswer cohAnswer;
    private QuestionService underTest;

    @Before
    public void setUp() {
        cohService = mock(CohService.class);
        cohQuestion = someCohQuestion();
        onlineHearingId = cohQuestion.getOnlineHearingId();
        questionId = cohQuestion.getQuestionId();
        cohAnswer = someCohAnswer();
        underTest = new QuestionService(cohService);
    }

    @Test
    public void getsAListOfQuestionsWhenThereIsOnlyOneRoundOfQuestions() {
        CohQuestionRounds cohQuestionRounds = someCohQuestionRoundsWithSingleRoundOfQuestions();
        CohQuestionReference cohQuestionReference1 = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0);
        CohQuestionReference cohQuestionReference2 = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(1);
        QuestionSummary question1Summary = new QuestionSummary(cohQuestionReference1.getQuestionId(),
                cohQuestionReference1.getQuestionHeaderText(), draft);
        QuestionSummary question2Summary = new QuestionSummary(cohQuestionReference2.getQuestionId(),
                cohQuestionReference2.getQuestionHeaderText(), draft);
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions.get(0), is(question1Summary));
        assertThat(questions.get(1), is(question2Summary));
    }

    @Test
    public void getsAListOfQuestionsWithDeadlineExpiryDateFromFirstQuestion() {
        CohQuestionRounds cohQuestionRounds = someCohQuestionRoundsWithSingleRoundOfQuestions();
        CohQuestionReference cohQuestion1Reference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0);
        CohQuestionReference cohQuestion2Reference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(1);
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId);

        assertThat(questionRound.getDeadlineExpiryDate(), is(cohQuestion1Reference.getDeadlineExpiryDate()));
        assertThat(questionRound.getDeadlineExpiryDate(), not(cohQuestion2Reference.getDeadlineExpiryDate()));
    }

    @Test
    public void getsAListOfQuestionsWithUnansweredStatesWhenTheQuestionHasNotBeenAnswered() {
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someQuestionId", 1, "first question", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), null)
                ))
        ));
        CohQuestionReference cohQuestionReference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0);
        String id = cohQuestionReference.getQuestionId();
        String questionHeaderText = cohQuestionReference.getQuestionHeaderText();
        QuestionSummary questionSummary = new QuestionSummary(id, questionHeaderText, unanswered);

        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);

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
        QuestionSummary questionSummary = new QuestionSummary(id, questionHeaderText, draft);
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(questionSummary));
    }

    @Test
    public void getsAListOfQuestionsInTheCorrectOrderWhenTheyAreReturnedInTheIncorrectOrder() {
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(new CohQuestionRound(
                asList(new CohQuestionReference("questionId2", 2, "second question", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted")),
                        new CohQuestionReference("questionId1", 1, "first question", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))))
        ));
        CohQuestionReference firstCohQuestionReference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(1);
        String firstQuestionId = firstCohQuestionReference.getQuestionId();
        String firstQuestionTitle = firstCohQuestionReference.getQuestionHeaderText();
        CohQuestionReference secondCohQuestionReference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0);
        String secondQuestionId = secondCohQuestionReference.getQuestionId();
        String secondQuestionTitle = secondCohQuestionReference.getQuestionHeaderText();
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(
                new QuestionSummary(firstQuestionId, firstQuestionTitle, draft),
                new QuestionSummary(secondQuestionId, secondQuestionTitle, draft)
                )
        );
    }

    @Test
    public void getsAQuestionWithAnAnswer() {
        when(cohService.getQuestion(onlineHearingId, questionId)).thenReturn(cohQuestion);
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(singletonList(cohAnswer));

        Question question = underTest.getQuestion(onlineHearingId, questionId);

        assertThat(question, is(new Question(cohQuestion.getOnlineHearingId(),
                cohQuestion.getQuestionId(),
                cohQuestion.getQuestionHeaderText(),
                cohQuestion.getQuestionBodyText(),
                cohAnswer.getAnswerText(),
                AnswerState.of(cohAnswer.getCurrentAnswerState().getStateName()),
                cohAnswer.getCurrentAnswerState().getStateDateTime()))
        );
    }

    @Test
    public void getsAQuestionWithoutAnAnswer() {
        when(cohService.getQuestion(onlineHearingId, questionId)).thenReturn(cohQuestion);
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(emptyList());

        Question question = underTest.getQuestion(onlineHearingId, questionId);

        assertThat(question, is(new Question(cohQuestion.getOnlineHearingId(),
                cohQuestion.getQuestionId(),
                cohQuestion.getQuestionHeaderText(),
                cohQuestion.getQuestionBodyText(),
                null,
                AnswerState.unanswered,
                null))
        );
    }

    @Test
    public void doesNotFindQuestion() {
        when(cohService.getQuestion(onlineHearingId, questionId)).thenReturn(null);

        Question question = underTest.getQuestion(onlineHearingId, questionId);

        assertThat(question, is(nullValue()));
        verify(cohService, never()).getAnswers(onlineHearingId, questionId);
    }

    @Test
    public void updateAnswerWhenQuestionHasNotAlreadyBeenAnswered() {
        String newAnswer = "new answer";
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(emptyList());
        underTest.updateAnswer(onlineHearingId, questionId, newAnswer);

        verify(cohService).createAnswer(onlineHearingId, questionId, new CohUpdateAnswer("answer_drafted", newAnswer));
    }

    @Test
    public void updateAnswerWhenQuestionHasAlreadyBeenAnswered() {
        String newAnswer = "new answer";
        String answerId = "some-id";
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(
                singletonList(new CohAnswer(answerId, "original answer", someCohState("answer_draQuestionService.javafted")))
        );
        underTest.updateAnswer(onlineHearingId, questionId, newAnswer);

        verify(cohService).updateAnswer(onlineHearingId, questionId, answerId, new CohUpdateAnswer("answer_drafted", newAnswer));
    }

    @Test
    public void submitAnswer() {
        String answerId = "some-id";
        String answer = "answer";
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(
                singletonList(new CohAnswer(answerId, answer, someCohState("answer_drafted")))
        );

        boolean hasBeenSubmitted = underTest.submitAnswer(onlineHearingId, questionId);

        assertThat(hasBeenSubmitted, is(true));
        verify(cohService).updateAnswer(onlineHearingId, questionId, answerId, new CohUpdateAnswer(submitted.getCohAnswerState(), answer));
    }

    @Test
    public void cannotSubmitAnswerThatHasNotAlreadyBeenAnswered() {
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(emptyList());

        boolean hasBeenSubmitted = underTest.submitAnswer(onlineHearingId, questionId);

        assertThat(hasBeenSubmitted, is(false));
        verify(cohService, never()).updateAnswer(any(), any(), any(), any());
    }
}