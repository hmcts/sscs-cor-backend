package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someQuestion;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;

public class QuestionServiceTest {
    private CohClient cohClient;

    @Before
    public void setUp() {
        cohClient = mock(CohClient.class);
    }

    @Test
    public void callsQuestionService() {
        Question expectedQuestion = someQuestion();
        when(cohClient.getQuestion(
                expectedQuestion.getOnlineHearingId(), expectedQuestion.getQuestionId())
        ).thenReturn(expectedQuestion);

        Question question = new QuestionService(cohClient).getQuestion(
                expectedQuestion.getOnlineHearingId(), expectedQuestion.getQuestionId()
        );

        assertThat(question, is(expectedQuestion));
    }
}