package uk.gov.hmcts.reform.sscscorbackend.domain;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohQuestion;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohState;

import java.util.HashMap;
import java.util.List;
import org.junit.Test;

public class CohConversationTest {
    @Test
    public void getIssueDate() {
        String issueDate = "someIssueDate";
        List<CohState> history = asList(new CohState("question_issued", issueDate), someCohState("someOtherState"));
        CohConversation cohConversation = new CohConversation(asList(someCohQuestion(1, history), someCohQuestion()));

        assertThat(cohConversation.getRoundIssueDates(), is(singletonMap(1, issueDate)));
    }

    @Test
    public void getMultipleIssueDate() {
        String issueDate1 = "someIssueDate1";
        String issueDate2 = "someIssueDate2";
        List<CohState> history1 = asList(new CohState("question_issued", issueDate1), someCohState("someOtherState"));
        List<CohState> history2 = asList(new CohState("question_issued", issueDate2), someCohState("someOtherState"));
        CohConversation cohConversation = new CohConversation(asList(someCohQuestion(1, history1), someCohQuestion(), someCohQuestion(2, history2), someCohQuestion()));

        HashMap<Integer, String> issueDates = new HashMap<Integer, String>();
        issueDates.put(1, issueDate1);
        issueDates.put(2, issueDate2);

        assertThat(cohConversation.getRoundIssueDates(), is(issueDates));
    }


    @Test
    public void getIssueDateWhenQuestionsHaveNotBeenIssued() {
        List<CohState> history = singletonList(someCohState("someOtherState"));
        CohConversation cohConversation = new CohConversation(asList(someCohQuestion(1, history), someCohQuestion()));

        assertThat(cohConversation.getRoundIssueDates(), is(emptyMap()));
    }

    @Test
    public void getIssueDateWhenNoQuestions() {
        CohConversation cohConversation = new CohConversation(emptyList());

        assertThat(cohConversation.getRoundIssueDates(), is(emptyMap()));
    }

    @Test
    public void getIssueDateWhenQuestionsIsNull() {
        CohConversation cohConversation = new CohConversation(null);

        assertThat(cohConversation.getRoundIssueDates(), is(emptyMap()));
    }
}