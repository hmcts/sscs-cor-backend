package uk.gov.hmcts.reform.sscscorbackend.domain;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohQuestion;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohState;

import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class CohQuestionTest {
    @Test
    public void getQuestionIssueDateFromHistory() {
        String issueDate = "issueDate";
        List<CohState> history = asList(new CohState("question_issued", issueDate), someCohState("someOtherState"));
        CohQuestion cohQuestion = someCohQuestion(1, history);

        assertThat(cohQuestion.getIssueDate(), is(Optional.of(issueDate)));
    }

    @Test
    public void getQuestionIssueDateWhenQuestionHasNotBeenIssued() {
        List<CohState> history = singletonList(someCohState("someOtherState"));
        CohQuestion cohQuestion = someCohQuestion(1, history);

        assertThat(cohQuestion.getIssueDate(), is(Optional.empty()));
    }

    @Test
    public void getQuestionIssueDateWhenQuestionHistoryEmpty() {
        List<CohState> history = emptyList();
        CohQuestion cohQuestion = someCohQuestion(1, history);

        assertThat(cohQuestion.getIssueDate(), is(Optional.empty()));
    }

    @Test
    public void getQuestionIssueDateWhenQuestionHistoryNull() {
        List<CohState> history = null;
        CohQuestion cohQuestion = someCohQuestion(1, history);

        assertThat(cohQuestion.getIssueDate(), is(Optional.empty()));
    }

}