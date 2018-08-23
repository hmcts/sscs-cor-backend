package uk.gov.hmcts.reform.sscscorbackend;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.draft;
import static uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRoleCoh.DISABILITY_QUALIFIED_MEMBER;
import static uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRoleCoh.JUDGE;
import static uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRoleCoh.MEDICAL_MEMBER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.*;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.*;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRequest;


public class DataFixtures {
    private DataFixtures() {}

    public static QuestionRound someQuestionRound() {
        return new QuestionRound(someQuestionSummaries());
    }

    public static List<QuestionSummary> someQuestionSummaries() {
        return singletonList(new QuestionSummary("someQuestionId", "someQuestionHeader", draft));
    }

    public static Question someQuestion() {
        return new Question("someHearingId", "someQuestionId", "someQuestionHeader", "someBody", "someAnswer");
    }

    public static CohQuestion someCohQuestion() {
        return new CohQuestion("someHearingId", "someQuestionId", "someHeader", "someBody");
    }

    public static CohAnswer someCohAnswer() {
        return new CohAnswer("answerId", "Some answer", someCohState("answer_drafted"));
    }

    public static List<CohAnswer> someCohAnswers() {
        return Collections.singletonList(new CohAnswer("answerId", "Some answer", someCohState("answer_drafted")));
    }

    public static CohState someCohState(String state) {
        return new CohState(state);
    }

    public static CohQuestionRounds someCohQuestionRoundsWithSingleRoundOfQuestions() {
        return new CohQuestionRounds(1, singletonList(new CohQuestionRound(singletonList(
                new CohQuestionReference("someQuestionId", 1, "first question", someCohAnswers())))));
    }

    public static CohQuestionRounds someCohQuestionRoundsMultipleRoundsOfQuestions() {
        return new CohQuestionRounds(2, Arrays.asList(
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someQuestionId", 1, "first round question", someCohAnswers()))),
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someOtherQuestionId", 1, "second round question", someCohAnswers())))
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

}
