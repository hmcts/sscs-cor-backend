package uk.gov.hmcts.reform.sscscorbackend;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.List;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;

public class DataFixtures {
    private DataFixtures() {}

    public static QuestionRound someQuestionRound() {
        return new QuestionRound(someQuestionSummaries());
    }

    public static List<QuestionSummary> someQuestionSummaries() {
        return singletonList(new QuestionSummary("someQuestionId", "someQuestionHeader"));
    }

    public static Question someQuestion() {
        return new Question("someHearingId", "someQuestionId", "someQuestionHeader", "someBody", "someAnswer");
    }

    public static CohQuestion someCohQuestion() {
        return new CohQuestion("someHearingId", "someQuestionId", "someHeader", "someBody");
    }

    public static CohAnswer someCohAnswer() {
        return new CohAnswer("answerId", "Some answer");
    }

    public static CohQuestionRounds someCohQuestionRoundsWithSingleRoundOfQuestions() {
        return new CohQuestionRounds(1, singletonList(new CohQuestionRound(singletonList(new CohQuestionReference("someQuestionId", 1, "first question")))));
    }

    public static CohQuestionRounds someCohQuestionRoundsMultipleRoundsOfQuestions() {
        return new CohQuestionRounds(2, Arrays.asList(
                new CohQuestionRound(singletonList(new CohQuestionReference("someQuestionId", 1, "first round question"))),
                new CohQuestionRound(singletonList(new CohQuestionReference("someOtherQuestionId", 1, "second round question")))
        ));
    }
}
