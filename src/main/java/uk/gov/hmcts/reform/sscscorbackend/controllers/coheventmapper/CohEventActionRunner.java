package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

@Component
@Slf4j
public class CohEventActionRunner {
    private final NotificationsService notificationService;

    public CohEventActionRunner(NotificationsService notificationService) {
        this.notificationService = notificationService;
    }

    public void runActionSync(CohEvent event, CohEventAction cohEventAction) {
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

    @Async
    public void runActionAsync(CohEvent event, CohEventAction cohEventAction) {
        runActionSync(event, cohEventAction);
    }
}
