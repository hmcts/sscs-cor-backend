package uk.gov.hmcts.reform.sscscorbackend;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Arrays.asList;
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
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfQuestion;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfQuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;
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
        return new CohQuestion("someHearingId", 1, "someQuestionId", 1, "someHeader", "someBody", emptyList(), emptyList());
    }

    public static CohQuestion someCohQuestion(CohAnswer answer) {
        return new CohQuestion("someHearingId", 1, "someQuestionId", 1, "someHeader", "someBody", emptyList(), singletonList(answer));
    }

    public static CohQuestion someCohQuestion(int questionRound, List<CohState> history) {
        return new CohQuestion("someOnlineHearingid", questionRound, "someQuestionId", 1, "questionHeaderText", "questionBodyText", history, emptyList());
    }

    public static CohAnswer someCohAnswer() {
        return new CohAnswer("answerId", "Some answer", someCohState("answer_drafted"), emptyList());
    }

    public static List<CohAnswer> someCohAnswers(String state) {
        return singletonList(new CohAnswer("answerId", "Some answer", someCohState(state), emptyList()));
    }

    public static CohState someCohState(String state) {
        return new CohState(state, "2018-08-08T09:12:12Z");
    }

    public static CohQuestionRounds someCohQuestionRoundsWithSingleRoundOfQuestions() {
        List<CohQuestionReference> cohQuestionReferenceList = Arrays.asList(
                new CohQuestionReference("someQuestionId1", 1, "first question", "first question body",  now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted")),
                new CohQuestionReference("someQuestionId2", 2, "second question", "second question body",  now().plusDays(10).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))
        );
        return new CohQuestionRounds(1, singletonList(new CohQuestionRound(cohQuestionReferenceList, 0)));
    }

    public static CohQuestionRounds someUnpublishedCohQuestionRounds() {
        return new CohQuestionRounds(0, Collections.emptyList());
    }

    public static CohQuestionRounds someCohQuestionRoundsMultipleRoundsOfQuestions() {
        return new CohQuestionRounds(2, Arrays.asList(
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someQuestionId", 1, "first round question", "first question body", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))), 0),
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someOtherQuestionId", 1, "second round question","second question body",  now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))), 0)
        ));
    }

    public static CreateOnlineHearingRequest someRequest() {
        return new CreateOnlineHearingRequest("aCaseId");
    }

    public static CcdEvent someCcdEvent(String caseId) {
        CaseData caseData = new CaseData(null);

        CaseDetails caseDetails = new CaseDetails(caseId, caseData);
        return new CcdEvent(caseDetails);
    }

    public static CohEvent someCohEvent(String caseId, String hearingId, String event) {
        return new CohEvent(caseId, hearingId, event, null, null);
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
        Decision decision = new Decision("decision_issued", now().format(ISO_LOCAL_DATE_TIME), null, null, "startDate", "endDate", null, "decisionReason", null);
        return new OnlineHearing("someOnlineHearingId", "someAppellantName", "someCaseReference", decision);
    }

    public static OnlineHearing someOnlineHearingWithDecisionAndAppellentReply() {
        Decision decision = new Decision("decision_issued", now().format(ISO_LOCAL_DATE_TIME), "decision_accepted", now().format(ISO_LOCAL_DATE_TIME), "startDate", "endDate", null, "decisionReason", null);
        return new OnlineHearing("someOnlineHearingId", "someAppellantName", "someCaseReference", decision);
    }

    public static Evidence someEvidence() {
        return new Evidence("http://example.com/document/1", "someFilename.txt", "2018-10-24'T'12:11:21Z");
    }

    public static PdfSummary somePdfSummary() {
        return new PdfSummary(somePdfAppealDetails(),
                singletonList(
                        new PdfQuestionRound(singletonList(
                                new PdfQuestion("title", "body", "answer", "issueDate", "submittedDate")
                        ))
                )
        );
    }

    public static PdfAppealDetails somePdfAppealDetails() {
        return new PdfAppealDetails("someTitle", "someFirstName", "someSurname", "someNino", "someCaseRef");
    }

    public static Decision someDecision() {
        return new Decision(
                "decisionsState",
                "decisionsStartDateTime",
                "appellantReply",
                "appellantReplyDateTime",
                "2017-04-01",
                "2018-12-11",
                new DecisionRates(Rate.noAward, Rate.enhancedRate, ComparedRate.Higher),
                "There was a reason!",
                new Activities(
                        asList(new Activity("mobilityActivity1", "2.1"), new Activity("mobilityActivity2", "7.5")), asList(new Activity("dailyActivity1", "3.2"), new Activity("dailyActivity2", "4"))
                )
        );
    }
}
