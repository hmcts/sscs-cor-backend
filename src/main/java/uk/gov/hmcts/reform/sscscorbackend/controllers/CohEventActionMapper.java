package uk.gov.hmcts.reform.sscscorbackend.controllers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionRoundIssuedService;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingTribunalsViewService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

@Service
public class CohEventActionMapper {
    private final Map<String, Action> actions;

    @Autowired
    public CohEventActionMapper(StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService,
                                NotificationsService notificationsService,
                                QuestionRoundIssuedService questionRoundIssuedService,
                                StoreOnlineHearingService storeOnlineHearingService) {
        this(buildActionsMap(storeOnlineHearingTribunalsViewService, notificationsService, questionRoundIssuedService, storeOnlineHearingService));
    }

    CohEventActionMapper(HashMap<String, Action> actions) {
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

    private static HashMap<String, Action> buildActionsMap(
            StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService,
            NotificationsService notificationsService,
            QuestionRoundIssuedService questionRoundIssuedService,
            StoreOnlineHearingService storeOnlineHearingService
    ) {
        HashMap<String, Action> actions = new HashMap<>();
        actions.put("decision_issued", (caseId, onlineHearingId, cohEvent) -> {
            storeOnlineHearingTribunalsViewService.storePdf(caseId, onlineHearingId);
            notificationsService.send(cohEvent);
        });
        actions.put("question_round_issued", (caseId, onlineHearingId, cohEvent) ->
            questionRoundIssuedService.handleQuestionRoundIssued(cohEvent)
        );
        actions.put("continuous_online_hearing_relisted",  (caseId, onlineHearingId, cohEvent) ->
            storeOnlineHearingService.storePdf(caseId, onlineHearingId)
        );

        return actions;
    }

    @FunctionalInterface
    interface Action {
        public void handle(Long caseId, String onlineHearingId, CohEvent cohEvent);
    }
}
