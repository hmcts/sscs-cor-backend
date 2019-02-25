package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingTribunalsViewService;
import uk.gov.hmcts.reform.sscscorbackend.service.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

@Service
public class DecisionIssuedEventAction implements CohEventAction {
    private final StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService;
    private final CorEmailService emailService;
    private final DwpEmailMessageBuilder dwpEmailMessageBuilder;

    public DecisionIssuedEventAction(StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService, CorEmailService emailService, DwpEmailMessageBuilder dwpEmailMessageBuilder) {
        this.storeOnlineHearingTribunalsViewService = storeOnlineHearingTribunalsViewService;
        this.emailService = emailService;
        this.dwpEmailMessageBuilder = dwpEmailMessageBuilder;
    }

    @Override
    public void handle(Long caseId, String onlineHearingId, StorePdfResult storePdfResult) {
        String caseReference = storePdfResult.getDocument().getData().getCaseReference();
        emailService.sendPdfToDwp(
                storePdfResult,
                "Preliminary view offered (" + caseReference + ")",
                dwpEmailMessageBuilder.getDecisionIssuedMessage(storePdfResult.getDocument())
        );
    }

    @Override
    public String eventCanHandle() {
        return "decision_issued";
    }

    @Override
    public StorePdfService getPdfService() {
        return storeOnlineHearingTribunalsViewService;
    }
}
