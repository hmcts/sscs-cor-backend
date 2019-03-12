package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

@Component
@Slf4j
public class CohEventActionRunner {
    private final CorCcdService corCcdService;
    private final IdamService idamService;
    private final NotificationsService notificationService;

    public CohEventActionRunner(CorCcdService corCcdService, IdamService idamService, NotificationsService notificationService) {
        this.corCcdService = corCcdService;
        this.idamService = idamService;
        this.notificationService = notificationService;
    }

    public void runActionSync(CohEvent event, CohEventAction cohEventAction) {
        String onlineHearingId = event.getOnlineHearingId();
        Long caseId = Long.valueOf(event.getCaseId());

        SscsCaseDetails sscsCaseDetails = loadCcdCaseDetails(caseId);
        log.info("Storing pdf [" + caseId + "]");
        CohEventActionContext storePdfResultStorePdf = cohEventAction.createAndStorePdf(caseId, onlineHearingId, sscsCaseDetails);
        log.info("Handle coh event [" + caseId + "]");
        CohEventActionContext cohEventActionContextHandle = cohEventAction.handle(caseId, onlineHearingId, storePdfResultStorePdf);
        log.info("Notify appellant [" + caseId + "]");

        if (cohEventAction.notifyAppellant()) {
            notificationService.send(event);
        }

        corCcdService.updateCase(
                cohEventActionContextHandle.getDocument().getData(),
                caseId,
                cohEventAction.getCcdEventType().getCcdType(),
                "SSCS COH - Event received",
                "Coh event [" + cohEventAction.cohEvent() + "] received",
                idamService.getIdamTokens());
    }

    @Async
    public void runActionAsync(CohEvent event, CohEventAction cohEventAction) {
        runActionSync(event, cohEventAction);
    }

    private SscsCaseDetails loadCcdCaseDetails(long caseId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        log.info("Loading case [" + caseId + "]");
        SscsCaseDetails caseDetails = corCcdService.getByCaseId(caseId, idamTokens);
        log.info("Loaded case [" + caseId + "]");

        return caseDetails;
    }
}
