package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public CohEventActionMapper(List<CohEventAction> actions, NotificationsService notificationService) {
        this.actions = actions;
        this.notificationService = notificationService;
        executorService = Executors.newFixedThreadPool(3);
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
            executorService.execute(() -> {
                String onlineHearingId = event.getOnlineHearingId();
                Long caseId = Long.valueOf(event.getCaseId());

                try {
                    log.info("Storing pdf [" + caseId + "]");
                    StorePdfResult storePdfResult = cohEventAction.getPdfService().storePdf(caseId, onlineHearingId);
                    log.info("Handle coh event [" + caseId + "]");
                    cohEventAction.handle(caseId, onlineHearingId, storePdfResult);
                    log.info("Notify appellant [" + caseId + "]");
                    if (cohEventAction.notifyAppellant()) {
                        notificationService.send(event);
                    }
                } catch (Exception exc) {
                    log.error("Error processing COH event for case id [" + caseId + "] online hearing id [" + onlineHearingId + "]", exc);
                }
            });
            return true;
        }
        return false;
    }
}
