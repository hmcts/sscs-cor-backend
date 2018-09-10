package uk.gov.hmcts.reform.sscscorbackend;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.draft;
import static uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRoleCoh.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CaseData;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.Panel;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRequest;


public class DataFixtures {
    private DataFixtures() {}

    public static QuestionRound someQuestionRound() {
        return new QuestionRound(someQuestionSummaries(), now().plusDays(7).format(ISO_LOCAL_DATE_TIME));
    }

    public static List<QuestionSummary> someQuestionSummaries() {
        return singletonList(new QuestionSummary("someQuestionId", "someQuestionHeader", draft));
    }

    public static Question someQuestion() {
        return new Question("someHearingId", "someQuestionId", "someQuestionHeader", "someBody", "someAnswer", draft, "2018-08-08T09:12:12Z");
    }

    public static CohQuestion someCohQuestion() {
        return new CohQuestion("someHearingId", "someQuestionId", "someHeader", "someBody");
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
        return new CohQuestionRounds(1, singletonList(new CohQuestionRound(cohQuestionReferenceList)));
    }

    public static CohQuestionRounds someCohQuestionRoundsMultipleRoundsOfQuestions() {
        return new CohQuestionRounds(2, Arrays.asList(
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someQuestionId", 1, "first round question", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted")))),
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someOtherQuestionId", 1, "second round question", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))))
        ));
    }

    public static CreateOnlineHearingRequest someRequest() {
        List<PanelRequest> panel = new ArrayList<>();

        PanelRequest panelRequestJ = new PanelRequest(JUDGE,
                "aJudge", JUDGE);
        panel.add(panelRequestJ);

        PanelRequest panelRequestM = new PanelRequest(MEDICAL_MEMBER,
                "medicalPerson", MEDICAL_MEMBER);
        panel.add(panelRequestM);
        PanelRequest panelRequestQ = new PanelRequest(DISABILITY_QUALIFIED_MEMBER,
                "qualifiedPerson", DISABILITY_QUALIFIED_MEMBER);
        panel.add(panelRequestQ);

        return new CreateOnlineHearingRequest("aCaseId", panel);
    }

    public static Panel somePanel() {
        return new Panel("aJudge", "medicalPerson", "qualifiedPerson");
    }

    public static CcdEvent someCcdEvent(String caseId, Panel panel) {
        CaseData caseData = new CaseData(null, panel);

        CaseDetails caseDetails = new CaseDetails(caseId, caseData);
        CcdEvent ccdEvent = new CcdEvent(caseDetails);

        return ccdEvent;
    }


    public static CohOnlineHearings someCohOnlineHearingId() {
        return new CohOnlineHearings(singletonList(new CohOnlineHearing("someOnlineHearingId")));
    }

    public static OnlineHearing someOnlineHearing() {
        return new OnlineHearing("someOnlineHearingId", "someAppellantName", "someCaseReference");
    }
}
