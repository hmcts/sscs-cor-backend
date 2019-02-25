package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;

@Service
public class HearingRelistedAction implements CohEventAction {
    private final StoreOnlineHearingService storeOnlineHearingService;
    private final CorCcdService corCcdService;
    private final IdamService idamService;
    private final CorEmailService corEmailService;
    private final DwpEmailMessageBuilder dwpEmailMessageBuilder;

    @Autowired
    public HearingRelistedAction(StoreOnlineHearingService storeOnlineHearingService,
                                 CorCcdService corCcdService, IdamService idamService,
                                 CorEmailService corEmailService,
                                 DwpEmailMessageBuilder dwpEmailMessageBuilder) {
        this.storeOnlineHearingService = storeOnlineHearingService;
        this.corCcdService = corCcdService;
        this.idamService = idamService;
        this.corEmailService = corEmailService;
        this.dwpEmailMessageBuilder = dwpEmailMessageBuilder;
    }

    @Override
    public void handle(Long caseId, String onlineHearingId, StorePdfResult storePdfResult) {
        updateCcdCaseToOralHearing(caseId, storePdfResult);
        String relistedMessage = dwpEmailMessageBuilder.getRelistedMessage(storePdfResult.getDocument());
        corEmailService.sendEmailToDwp("COR: Hearing required", relistedMessage);
    }

    private void updateCcdCaseToOralHearing(Long caseId, StorePdfResult storePdfResult) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        SscsCaseData oralCaseData = updateHearingTypeToOral(storePdfResult.getDocument());
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

    @Override
    public String eventCanHandle() {
        return "continuous_online_hearing_relisted";
    }

    @Override
    public StorePdfService getPdfService() {
        return storeOnlineHearingService;
    }
}
