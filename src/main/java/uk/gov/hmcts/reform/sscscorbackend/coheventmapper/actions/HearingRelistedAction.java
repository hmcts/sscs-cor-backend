package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreOnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohConversations;

@Service
public class HearingRelistedAction implements CohEventAction {
    private final StoreOnlineHearingService storeOnlineHearingService;
    private final CorCcdService corCcdService;
    private final IdamService idamService;
    private final CorEmailService corEmailService;
    private final EmailMessageBuilder emailMessageBuilder;
    private final CohService cohService;

    @Autowired
    public HearingRelistedAction(StoreOnlineHearingService storeOnlineHearingService,
                                 CorCcdService corCcdService, IdamService idamService,
                                 CorEmailService corEmailService,
                                 EmailMessageBuilder emailMessageBuilder, CohService cohService) {
        this.storeOnlineHearingService = storeOnlineHearingService;
        this.corCcdService = corCcdService;
        this.idamService = idamService;
        this.corEmailService = corEmailService;
        this.emailMessageBuilder = emailMessageBuilder;
        this.cohService = cohService;
    }

    public CohEventActionContext handle(Long caseId, String onlineHearingId, SscsCaseDetails caseDetails) {
        CohEventActionContext actionContext = storeOnlineHearingService.storePdf(caseId, onlineHearingId, new PdfData(caseDetails));
        SscsCaseData oralSscsCaseData = updateCcdCaseToOralHearing(caseId, onlineHearingId, actionContext);
        String relistedMessage = emailMessageBuilder.getRelistedMessage(actionContext.getDocument());
        corEmailService.sendEmailToDwp("COR: Hearing required", relistedMessage);

        SscsCaseDetails oralSscsCaseDetails = actionContext.getDocument().toBuilder().data(oralSscsCaseData).build();
        return new CohEventActionContext(actionContext.getPdf(), oralSscsCaseDetails);
    }

    private SscsCaseData updateCcdCaseToOralHearing(Long caseId, String onlineHearingId, CohEventActionContext cohEventActionContext) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        SscsCaseDetails sscsCaseDetails = updateRelistingReason(onlineHearingId, cohEventActionContext.getDocument());
        SscsCaseData oralCaseData = updateHearingTypeToOral(sscsCaseDetails);
        corCcdService.updateCase(
                oralCaseData,
                caseId,
                EventType.UPDATE_HEARING_TYPE.getCcdType(),
                "SSCS - appeal updated event",
                "Update SSCS hearing type",
                idamTokens
        );
        return oralCaseData;
    }

    private SscsCaseDetails updateRelistingReason(String onlineHearingId, SscsCaseDetails sscsCaseDetails) {
        CohConversations conversations = cohService.getConversations(onlineHearingId);
        String relistingReason = conversations.getConversation().getRelisting().getReason();
        sscsCaseDetails.getData().setRelistingReason(relistingReason);

        return sscsCaseDetails;
    }

    private SscsCaseData updateHearingTypeToOral(SscsCaseDetails sscsCaseDetails) {
        SscsCaseData data = sscsCaseDetails.getData();
        return sscsCaseDetails.getData().toBuilder()
                .appeal(data.getAppeal().toBuilder().hearingType("oral").build())
                .build();
    }

    @Override
    public String cohEvent() {
        return "continuous_online_hearing_relisted";
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_ONLINE_HEARING_RELISTED;
    }
}
