package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

@Slf4j
@Service
public class CohEventActionMapper {
    private final List<CohEventAction> actions;
    private final NotificationsService notificationService;
    private final ExecutorService executorService;

    @Autowired
    public CohEventActionMapper(List<CohEventAction> actions,
                                NotificationsService notificationService,
                                @Value("${enable_coh_event_thread_pool}") Boolean enableCohEventThreadPool) {
        this.actions = actions;
        this.notificationService = notificationService;
        executorService = enableCohEventThreadPool ? Executors.newFixedThreadPool(3) : null;
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
            if (executorService != null) {
                executorService.execute(() -> {
                    String onlineHearingId = event.getOnlineHearingId();
                    String caseId = event.getCaseId();
                    try {
                        log.info("Handle COH event [" + event.getEventType() + "] " +
                                "in new thread for case id [" + caseId + "] " +
                                "online hearing id [" + onlineHearingId + "]");
                        runAction(event, cohEventAction);
                    } catch (Exception exc) {
                        log.error("Error processing COH event for case id [" + caseId + "] " +
                                "online hearing id [" + onlineHearingId + "]", exc);
                    }
                });
            } else {
                runAction(event, cohEventAction);
            }
            return true;
        }
        return false;
    }

    private void runAction(CohEvent event, CohEventAction cohEventAction) {
        String onlineHearingId = event.getOnlineHearingId();
        Long caseId = Long.valueOf(event.getCaseId());

        log.info("Storing pdf [" + caseId + "]");
        StorePdfResult storePdfResult = cohEventAction.getPdfService().storePdf(caseId, onlineHearingId);
        log.info("Handle coh event [" + caseId + "]");
        cohEventAction.handle(caseId, onlineHearingId, storePdfResult);
        log.info("Notify appellant [" + caseId + "]");
        if (cohEventAction.notifyAppellant()) {
            notificationService.send(event);
        }
    }

}
