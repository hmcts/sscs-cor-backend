package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someQuestion;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;

public class QuestionControllerTest {

    private QuestionService questionService;

    @Before
    public void setUp() {
        questionService = mock(QuestionService.class);
    }

    @Test
    public void getsAquestionBack() {
        Question expectedQuestion = someQuestion();
        when(questionService.getQuestion(
                expectedQuestion.getOnlineHearingId(), expectedQuestion.getQuestionId())
        ).thenReturn(expectedQuestion);

        ResponseEntity<Question> question = new QuestionController(questionService)
                .getQuestion(expectedQuestion.getOnlineHearingId(), expectedQuestion.getQuestionId());

        assertThat(question.getStatusCode(), is(HttpStatus.OK));
        assertThat(question.getBody(), is(expectedQuestion));
    }

    @Test
    public void cannotFindQuestion() {
        String unknownQuestionId = "unknown";
        String unknownHearingId = "unknown";
        when(questionService.getQuestion(unknownHearingId, unknownQuestionId)).thenReturn(null);

        ResponseEntity<Question> question =
                new QuestionController(questionService).getQuestion(unknownHearingId, unknownQuestionId);

        assertThat(question.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }
}
