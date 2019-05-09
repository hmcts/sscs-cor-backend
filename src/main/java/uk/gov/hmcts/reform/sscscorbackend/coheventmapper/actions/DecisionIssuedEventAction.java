package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreOnlineHearingTribunalsViewService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;

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
    public CohEventActionContext handle(Long caseId, String onlineHearingId, SscsCaseDetails caseDetails) {
        CohEventActionContext actionContext = storeOnlineHearingTribunalsViewService.storePdf(caseId, onlineHearingId, new PdfData(caseDetails));
        String caseReference = actionContext.getDocument().getData().getCaseReference();
        emailService.sendFileToDwp(
                actionContext,
                "Preliminary view offered (" + caseReference + ")",
                emailMessageBuilder.getDecisionIssuedMessage(actionContext.getDocument())
        );

        return actionContext;
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
