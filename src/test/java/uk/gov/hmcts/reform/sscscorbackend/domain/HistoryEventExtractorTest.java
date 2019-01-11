package uk.gov.hmcts.reform.sscscorbackend.domain;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohState;
import static uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.HistoryEventExtractor.getStateDate;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohState;

public class HistoryEventExtractorTest {

    private static final String QUESTION_ISSUED_STATE = "question_issued";

    @Test
    public void getQuestionIssueDateFromHistory() {
        String issueDate = "issueDate";
        List<CohState> history = asList(new CohState(QUESTION_ISSUED_STATE, issueDate), someCohState("someOtherState"));

        assertThat(getStateDate(history, "question_issued"), is(Optional.of(issueDate)));
    }

    @Test
    public void getQuestionIssueDateWhenQuestionHasNotBeenIssued() {
        List<CohState> history = singletonList(someCohState("someOtherState"));
        assertThat(getStateDate(history, "question_issued"), is(Optional.empty()));
    }

    @Test
    public void getQuestionIssueDateWhenQuestionHistoryEmpty() {
        List<CohState> history = emptyList();

        assertThat(getStateDate(history, QUESTION_ISSUED_STATE), is(Optional.empty()));
    }

    @Test
    public void getQuestionIssueDateWhenQuestionHistoryNull() {
        List<CohState> history = null;
        assertThat(getStateDate(history, QUESTION_ISSUED_STATE), is(Optional.empty()));
    }
}