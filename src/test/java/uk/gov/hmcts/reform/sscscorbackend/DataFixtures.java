package uk.gov.hmcts.reform.sscscorbackend;

import uk.gov.hmcts.reform.sscscorbackend.domain.CohAnswer;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohQuestion;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;

public class DataFixtures {
    private DataFixtures() {}

    public static Question someQuestion() {
        return new Question("someHearingId", "someQuestionId", "someHeader", "someBody", "someAnswer");
    }

    public static CohQuestion someCohQuestion() {
        return new CohQuestion("someHearingId", "someQuestionId", "someHeader", "someBody");
    }

    public static CohAnswer someCohAnswer() {
        return new CohAnswer("answerId", "Some answer");
    }
}
