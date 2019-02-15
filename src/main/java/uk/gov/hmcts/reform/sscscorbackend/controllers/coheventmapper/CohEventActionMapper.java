package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

@Service
public class CohEventActionMapper {
    private final List<CohEventAction> actions;
    private final NotificationsService notificationService;

    @Autowired
    public CohEventActionMapper(List<CohEventAction> actions, NotificationsService notificationService) {
        this.actions = actions;
        this.notificationService = notificationService;
    }

    private CohEventAction getActionFor(CohEvent event) {
        return actions.stream()
                .filter(action -> action.eventCanHandle().equals(event.getEventType()))
                .findFirst()
                .orElse(null);
    }

    public boolean handle(CohEvent event) {
        CohEventAction cohEventAction = getActionFor(event);
        if (cohEventAction != null) {
            String onlineHearingId = event.getOnlineHearingId();
            Long caseId = Long.valueOf(event.getCaseId());

            cohEventAction.handle(caseId, onlineHearingId);
            if (cohEventAction.notifyAppellant()) {
                notificationService.send(event);
            }
            return true;
        }
        return false;
    }
}
