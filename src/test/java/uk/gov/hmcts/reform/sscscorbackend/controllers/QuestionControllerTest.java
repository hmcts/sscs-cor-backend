package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someQuestion;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someQuestionRound;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscscorbackend.domain.Answer;
import uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;

public class QuestionControllerTest {

    private QuestionService questionService;
    private Question expectedQuestion;
    private String onlineHearingId;
    private String questionId;
    private QuestionController underTest;
    private QuestionRound questionRound;

    @Before
    public void setUp() {
        expectedQuestion = someQuestion();
        onlineHearingId = expectedQuestion.getOnlineHearingId();
        questionId = expectedQuestion.getQuestionId();
        questionRound = someQuestionRound();

        questionService = mock(QuestionService.class);

        underTest = new QuestionController(questionService);
    }

    @Test
    public void getsAListOfQuestions() {
        when(questionService.getQuestions(onlineHearingId)).thenReturn(questionRound);

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
        Answer newAnswer = new Answer(AnswerState.draft, "new answer");
        ResponseEntity response = underTest.updateAnswer(onlineHearingId, questionId, newAnswer);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
        verify(questionService).updateAnswer(onlineHearingId, questionId, newAnswer);
    }
}
