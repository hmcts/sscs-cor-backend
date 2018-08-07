package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;

public class QuestionServiceTest {
    private String someHearingId;
    private String someQuestionId;
    private CohClient cohClient;

    @Before
    public void setUp() {
        someHearingId = "someHearingId";
        someQuestionId = "someQuestionId";
        cohClient = mock(CohClient.class);
    }

    @Test
    public void callsQuestionService() {
        Question expectedQuestion = new Question();
        when(cohClient.getQuestion(someHearingId, someQuestionId)).thenReturn(expectedQuestion);

        Question question =
                new QuestionService(cohClient).getQuestion(someHearingId, someQuestionId);

        assertThat(question, is(expectedQuestion));

    }
}