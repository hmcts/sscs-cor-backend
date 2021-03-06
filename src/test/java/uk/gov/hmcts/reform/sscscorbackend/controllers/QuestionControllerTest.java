package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscscorbackend.domain.Answer;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.service.CaseNotCorException;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;

public class QuestionControllerTest {

    private QuestionService questionService;
    private Question expectedQuestion;
    private String onlineHearingId;
    private String questionId;
    private QuestionController underTest;
    private QuestionRound questionRound;
    private OnlineHearingService onlineHearingService;

    @Before
    public void setUp() {
        expectedQuestion = someQuestion();
        onlineHearingId = expectedQuestion.getOnlineHearingId();
        questionId = expectedQuestion.getQuestionId();
        questionRound = someQuestionRound();

        questionService = mock(QuestionService.class);
        onlineHearingService = mock(OnlineHearingService.class);

        underTest = new QuestionController(questionService, onlineHearingService);
    }

    @Test
    public void getsAnOnlineHearing() {
        String someEmailAddress = "someEmailAddress";
        OnlineHearing expectedOnlineHearing = someOnlineHearing();
        when(onlineHearingService.getOnlineHearing(someEmailAddress)).thenReturn(Optional.of(expectedOnlineHearing));

        ResponseEntity<OnlineHearing> onlineHearingResponse = underTest.getOnlineHearing(someEmailAddress);

        assertThat(onlineHearingResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(onlineHearingResponse.getBody(), is(expectedOnlineHearing));
    }

    @Test
    public void getsAnOnlineHearingWithDecision() {
        String someEmailAddress = "someEmailAddress";
        OnlineHearing expectedOnlineHearing = someOnlineHearingWithDecision();
        when(onlineHearingService.getOnlineHearing(someEmailAddress)).thenReturn(Optional.of(expectedOnlineHearing));

        ResponseEntity<OnlineHearing> onlineHearingResponse = underTest.getOnlineHearing(someEmailAddress);

        assertThat(onlineHearingResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(onlineHearingResponse.getBody(), is(expectedOnlineHearing));
    }

    @Test
    public void getsAnOnlineHearingWithDecisionAndAppellantReply() {
        String someEmailAddress = "someEmailAddress";
        OnlineHearing expectedOnlineHearing = someOnlineHearingWithDecisionAndAppellentReply();
        when(onlineHearingService.getOnlineHearing(someEmailAddress)).thenReturn(Optional.of(expectedOnlineHearing));

        ResponseEntity<OnlineHearing> onlineHearingResponse = underTest.getOnlineHearing(someEmailAddress);

        assertThat(onlineHearingResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(onlineHearingResponse.getBody(), is(expectedOnlineHearing));
    }

    @Test
    public void cannotFindOnlineHearing() {
        String someEmailAddress = "someEmailAddress";
        when(onlineHearingService.getOnlineHearing(someEmailAddress)).thenReturn(Optional.empty());

        ResponseEntity<OnlineHearing> onlineHearingResponse = underTest.getOnlineHearing(someEmailAddress);

        assertThat(onlineHearingResponse.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void foundAHearingButItIsNotACorCase() {
        String someEmailAddress = "someEmailAddress";
        when(onlineHearingService.getOnlineHearing(someEmailAddress))
                .thenThrow(new CaseNotCorException());

        ResponseEntity<OnlineHearing> onlineHearingResponse = underTest.getOnlineHearing(someEmailAddress);

        assertThat(onlineHearingResponse.getStatusCode(), is(HttpStatus.CONFLICT));
    }

    @Test
    public void foundMultipleHearings() {
        String someEmailAddress = "someEmailAddress";
        when(onlineHearingService.getOnlineHearing(someEmailAddress))
                .thenThrow(new IllegalStateException("Found multiple hearings"));

        ResponseEntity<OnlineHearing> onlineHearingResponse = underTest.getOnlineHearing(someEmailAddress);

        assertThat(onlineHearingResponse.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    public void getsAListOfQuestions() {
        when(questionService.getQuestions(onlineHearingId, true)).thenReturn(questionRound);

        ResponseEntity<QuestionRound> question = underTest.getQuestionList(onlineHearingId);

        assertThat(question.getStatusCode(), is(HttpStatus.OK));
        assertThat(question.getBody(), is(questionRound));
    }

    @Test
    public void getsAQuestionBack() {
        when(questionService.getQuestion(onlineHearingId, questionId)).thenReturn(expectedQuestion);

        ResponseEntity<Question> question = underTest
                .getQuestion(onlineHearingId, questionId);

        assertThat(question.getStatusCode(), is(HttpStatus.OK));
        assertThat(question.getBody(), is(expectedQuestion));
    }

    @Test
    public void cannotFindQuestion() {
        String unknownQuestionId = "unknown";
        String unknownHearingId = "unknown";
        when(questionService.getQuestion(unknownHearingId, unknownQuestionId)).thenReturn(null);

        ResponseEntity<Question> question = underTest.getQuestion(unknownHearingId, unknownQuestionId);

        assertThat(question.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void updateAnswer() {
        String newAnswer = "new answer";
        ResponseEntity response = underTest.updateAnswer(onlineHearingId, questionId, new Answer(newAnswer));

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
        verify(questionService).updateAnswer(onlineHearingId, questionId, newAnswer);
    }

    @Test
    public void submitsAnswer() {
        when(questionService.submitAnswer(onlineHearingId, questionId)).thenReturn(true);
        ResponseEntity response = underTest.submitAnswer(onlineHearingId, questionId);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }

    @Test
    public void cannotFindAnswerToSubmit() {
        when(questionService.submitAnswer(onlineHearingId, questionId)).thenReturn(false);
        ResponseEntity response = underTest.submitAnswer(onlineHearingId, questionId);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void canExtendDeadline() {
        when(questionService.extendQuestionRoundDeadline(onlineHearingId)).thenReturn(Optional.of(questionRound));

        ResponseEntity<QuestionRound> response = underTest.extendQuestionRoundDeadline(onlineHearingId);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(questionRound));
    }

    @Test
    public void cannotExtendDeadline() {
        when(questionService.extendQuestionRoundDeadline(onlineHearingId)).thenReturn(Optional.empty());

        ResponseEntity<QuestionRound> response = underTest.extendQuestionRoundDeadline(onlineHearingId);

        assertThat(response.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
    }
}
