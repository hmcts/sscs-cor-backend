package uk.gov.hmcts.reform.sscscorbackend;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.draft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CaseData;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;


public class DataFixtures {
    private DataFixtures() {}

    public static QuestionRound someQuestionRound() {
        return new QuestionRound(someQuestionSummaries(), now().plusDays(7).format(ISO_LOCAL_DATE_TIME), 0);
    }

    public static List<QuestionSummary> someQuestionSummaries() {
        return singletonList(new QuestionSummary("someQuestionId", 1, "someQuestionHeader", draft));
    }

    public static Question someQuestion() {
        return new Question("someHearingId", "someQuestionId", 1, "someQuestionHeader", "someBody", "someAnswer", draft, "2018-08-08T09:12:12Z", emptyList());
    }

    public static CohQuestion someCohQuestion() {
        return new CohQuestion("someHearingId", "someQuestionId", 1, "someHeader", "someBody");
    }

    public static CohAnswer someCohAnswer() {
        return new CohAnswer("answerId", "Some answer", someCohState("answer_drafted"));
    }

    public static List<CohAnswer> someCohAnswers(String state) {
        return Collections.singletonList(new CohAnswer("answerId", "Some answer", someCohState(state)));
    }

    public static CohState someCohState(String state) {
        return new CohState(state, "2018-08-08T09:12:12Z");
    }

    public static CohQuestionRounds someCohQuestionRoundsWithSingleRoundOfQuestions() {
        List<CohQuestionReference> cohQuestionReferenceList = Arrays.asList(
                new CohQuestionReference("someQuestionId1", 1, "first question", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted")),
                new CohQuestionReference("someQuestionId2", 2, "second question", now().plusDays(10).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))
        );
        return new CohQuestionRounds(1, singletonList(new CohQuestionRound(cohQuestionReferenceList, 0)));
    }

    public static CohQuestionRounds someCohQuestionRoundsMultipleRoundsOfQuestions() {
        return new CohQuestionRounds(2, Arrays.asList(
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someQuestionId", 1, "first round question", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))), 0),
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someOtherQuestionId", 1, "second round question", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))), 0)
        ));
    }

    public static CreateOnlineHearingRequest someRequest() {
        return new CreateOnlineHearingRequest("aCaseId");
    }

    public static CcdEvent someCcdEvent(String caseId) {
        CaseData caseData = new CaseData(null);

        CaseDetails caseDetails = new CaseDetails(caseId, caseData);
        CcdEvent ccdEvent = new CcdEvent(caseDetails);

        return ccdEvent;
    }


    public static CohOnlineHearings someCohOnlineHearings() {
        return new CohOnlineHearings(singletonList(someCohOnlineHearing()));
    }

    public static CohOnlineHearing someCohOnlineHearing() {
        return new CohOnlineHearing("someOnlineHearingId", "123");
    }

    public static OnlineHearing someOnlineHearing() {
        return new OnlineHearing("someOnlineHearingId", "someAppellantName", "someCaseReference", null);
    }

    public static OnlineHearing someOnlineHearingWithDecision() {
        Decision decision = new Decision("someOnlineHearingId", "decisionAward", "decisionHeader", "decisionReason", "decisionText", "decision_issued", now().format(ISO_LOCAL_DATE_TIME), null, null);
        return new OnlineHearing("someOnlineHearingId", "someAppellantName", "someCaseReference", decision);
    }

    public static OnlineHearing someOnlineHearingWithDecisionAndAppellentReply() {
        Decision decision = new Decision("someOnlineHearingId", "decisionAward", "decisionHeader", "decisionReason", "decisionText", "decision_issued", now().format(ISO_LOCAL_DATE_TIME), "decision_accepted", now().format(ISO_LOCAL_DATE_TIME));
        return new OnlineHearing("someOnlineHearingId", "someAppellantName", "someCaseReference", decision);
    }

    public static Evidence someEvidence() {
        return new Evidence("http://example.com/document/1", "someFilename.txt", "2018-10-24'T'12:11:21Z");
    }
}
