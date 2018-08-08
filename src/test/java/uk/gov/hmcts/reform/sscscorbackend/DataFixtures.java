package uk.gov.hmcts.reform.sscscorbackend;

import uk.gov.hmcts.reform.sscscorbackend.domain.Question;

public class DataFixtures {
    private DataFixtures() {}

    public static Question someQuestion() {
        return new Question("someHearingId", "someQuestionId", "someHeader", "someBody");
    }
}
