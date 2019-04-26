package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingTribunalsViewService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.PdfData;

@Service
public class DecisionIssuedEventAction implements CohEventAction {
    private final StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService;
    private final CorEmailService emailService;
    private final EmailMessageBuilder emailMessageBuilder;

    public DecisionIssuedEventAction(StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService, CorEmailService emailService, EmailMessageBuilder emailMessageBuilder) {
        this.storeOnlineHearingTribunalsViewService = storeOnlineHearingTribunalsViewService;
        this.emailService = emailService;
        this.emailMessageBuilder = emailMessageBuilder;
    }

    @Override
    public CohEventActionContext createAndStorePdf(Long caseId, String onlineHearingId, SscsCaseDetails caseDetails) {
        return storeOnlineHearingTribunalsViewService.storePdf(caseId, onlineHearingId, new PdfData(caseDetails));
    }

    @Override
    public CohEventActionContext handle(Long caseId, String onlineHearingId, CohEventActionContext cohEventActionContext) {
        String caseReference = cohEventActionContext.getDocument().getData().getCaseReference();
        emailService.sendPdfToDwp(
                cohEventActionContext,
                "Preliminary view offered (" + caseReference + ")",
                emailMessageBuilder.getDecisionIssuedMessage(cohEventActionContext.getDocument())
        );

        return cohEventActionContext;
    }

    @Override
    public String cohEvent() {
        return "decision_issued";
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_DECISION_ISSUED;
    }
}
