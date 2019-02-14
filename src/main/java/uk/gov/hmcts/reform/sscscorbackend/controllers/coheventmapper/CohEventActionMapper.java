package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionRoundIssuedService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;

@Service
public class CohEventActionMapper {
    private final Map<String, CohEventAction> actions;

    @Autowired
    public CohEventActionMapper(QuestionRoundIssuedService questionRoundIssuedService,
                                HearingRelistedAction hearingRelistedAction,
                                AnswerSubmittedEventAction answerSubmittedEventAction,
                                DecisionIssuedEventAction decisionIssuedEventAction) {
        this(buildActionsMap(questionRoundIssuedService,
                hearingRelistedAction, answerSubmittedEventAction, decisionIssuedEventAction));
    }

    CohEventActionMapper(HashMap<String, CohEventAction> actions) {
        this.actions = actions;
    }

    private boolean canHandleEvent(CohEvent event) {
        return actions.containsKey(event.getEventType());
    }

    public boolean handle(CohEvent event) {
        if (canHandleEvent(event)) {
            String onlineHearingId = event.getOnlineHearingId();
            Long caseId = Long.valueOf(event.getCaseId());

            actions.get(event.getEventType()).handle(caseId, onlineHearingId, event);

            return true;
        }
        return false;
    }

    private static HashMap<String, CohEventAction> buildActionsMap(
            QuestionRoundIssuedService questionRoundIssuedService,
            HearingRelistedAction hearingRelistedAction,
            AnswerSubmittedEventAction answerSubmittedEventAction,
            DecisionIssuedEventAction decisionIssuedEventAction) {
        HashMap<String, CohEventAction> actions = new HashMap<>();
        actions.put("decision_issued", decisionIssuedEventAction);
        actions.put("question_round_issued", (caseId, onlineHearingId, cohEvent) ->
            questionRoundIssuedService.handleQuestionRoundIssued(cohEvent)
        );
        actions.put("continuous_online_hearing_relisted", hearingRelistedAction);
        actions.put("answers_submitted", answerSubmittedEventAction);

        return actions;
    }

}
