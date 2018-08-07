package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;

public class QuestionControllerTest {

    private String someHearingId;
    private String someQuestionId;
    private QuestionService questionService;

    @Before
    public void setUp() {
        someHearingId = "someHearingId";
        someQuestionId = "someQuestionId";
        questionService = mock(QuestionService.class);
    }

    @Test
    public void callsQuestionService() {
        Question expectedQuestion = new Question();
        when(questionService.getQuestion(someHearingId, someQuestionId)).thenReturn(expectedQuestion);

        ResponseEntity<Question> question =
                new QuestionController(questionService).getQuestion(someHearingId, someQuestionId);

        assertThat(question.getStatusCode(), is(HttpStatus.OK));
        assertThat(question.getBody(), is(expectedQuestion));
    }
}
