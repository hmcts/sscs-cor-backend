package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

@Service
public class HearingRelistedAction implements CohEventAction {
    private final StoreOnlineHearingService storeOnlineHearingService;
    private final NotificationsService notificationsService;
    private final CorCcdService corCcdService;
    private final IdamService idamService;

    @Autowired
    public HearingRelistedAction(StoreOnlineHearingService storeOnlineHearingService,
                                 NotificationsService notificationsService,
                                 CorCcdService corCcdService, IdamService idamService) {
        this.storeOnlineHearingService = storeOnlineHearingService;
        this.notificationsService = notificationsService;
        this.corCcdService = corCcdService;
        this.idamService = idamService;
    }

    @Override
    public void handle(Long caseId, String onlineHearingId, CohEvent cohEvent) {
        storeOnlineHearingService.storePdf(caseId, onlineHearingId);
        notificationsService.send(cohEvent);
        updateCcdCaseToOralHearing(caseId);
    }

    private void updateCcdCaseToOralHearing(Long caseId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        SscsCaseDetails sscsCaseDetails = corCcdService.getByCaseId(caseId, idamTokens);
        SscsCaseData oralCaseData = updateHearingTypeToOral(sscsCaseDetails);
        corCcdService.updateCase(
                oralCaseData,
                caseId,
                EventType.UPDATE_HEARING_TYPE.getCcdType(),
                "SSCS - appeal updated event",
                "Update SSCS hearing type",
                idamTokens
        );
    }

    private SscsCaseData updateHearingTypeToOral(SscsCaseDetails sscsCaseDetails) {
        SscsCaseData data = sscsCaseDetails.getData();
        return sscsCaseDetails.getData().toBuilder()
                .appeal(data.getAppeal().toBuilder().hearingType("oral").build())
                .build();
    }
}
