package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

@Service
public class CohEventActionMapper {
    private final Map<String, CohEventAction> actions;
    private final NotificationsService notificationService;

    @Autowired
    public CohEventActionMapper(QuestionRoundIssuedEventAction questionRoundIssuedEventAction,
                                HearingRelistedAction hearingRelistedAction,
                                AnswerSubmittedEventAction answerSubmittedEventAction,
                                DecisionIssuedEventAction decisionIssuedEventAction,
                                NotificationsService notificationsService) {
        this(buildActionsMap(questionRoundIssuedEventAction,
                hearingRelistedAction,
                answerSubmittedEventAction,
                decisionIssuedEventAction),
                notificationsService
        );
    }

    CohEventActionMapper(HashMap<String, CohEventAction> actions, NotificationsService notificationService) {
        this.actions = actions;
        this.notificationService = notificationService;
    }

    private boolean canHandleEvent(CohEvent event) {
        return actions.containsKey(event.getEventType());
    }

    public boolean handle(CohEvent event) {
        if (canHandleEvent(event)) {
            String onlineHearingId = event.getOnlineHearingId();
            Long caseId = Long.valueOf(event.getCaseId());

            CohEventAction cohEventAction = actions.get(event.getEventType());
            cohEventAction.handle(caseId, onlineHearingId);
            if (cohEventAction.notifyAppellant()) {
                notificationService.send(event);
            }
            return true;
        }
        return false;
    }

    private static HashMap<String, CohEventAction> buildActionsMap(
            QuestionRoundIssuedEventAction questionRoundIssuedEventAction,
            HearingRelistedAction hearingRelistedAction,
            AnswerSubmittedEventAction answerSubmittedEventAction,
            DecisionIssuedEventAction decisionIssuedEventAction) {
        HashMap<String, CohEventAction> actions = new HashMap<>();
        actions.put("decision_issued", decisionIssuedEventAction);
        actions.put("question_round_issued", questionRoundIssuedEventAction);
        actions.put("continuous_online_hearing_relisted", hearingRelistedAction);
        actions.put("answers_submitted", answerSubmittedEventAction);

        return actions;
    }

}
