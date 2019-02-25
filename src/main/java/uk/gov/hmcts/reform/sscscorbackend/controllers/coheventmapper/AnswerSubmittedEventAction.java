package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreAnswersPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

@Service
public class AnswerSubmittedEventAction implements CohEventAction {
    private final CorEmailService corEmailService;
    private final StorePdfService storeAnswersPdfService;
    private final DwpEmailMessageBuilder dwpEmailMessageBuilder;

    public AnswerSubmittedEventAction(CorEmailService corEmailService, StoreAnswersPdfService storeAnswersPdfService, DwpEmailMessageBuilder dwpEmailMessageBuilder) {
        this.corEmailService = corEmailService;
        this.storeAnswersPdfService = storeAnswersPdfService;
        this.dwpEmailMessageBuilder = dwpEmailMessageBuilder;
    }

    @Override
    public CohEventActionContext createAndStorePdf(Long caseId, String onlineHearingId, SscsCaseDetails caseDetails) {
        return storeAnswersPdfService.storePdf(caseId, onlineHearingId, caseDetails);
    }

    @Override
    public CohEventActionContext handle(Long caseId, String onlineHearingId, CohEventActionContext cohEventActionContext) {
        SscsCaseDetails sscsCaseDetails = cohEventActionContext.getDocument();
        String caseReference = sscsCaseDetails.getData().getCaseReference();
        corEmailService.sendPdfToDwp(
                cohEventActionContext,
                "Appellant has provided information (" + caseReference + ")",
                dwpEmailMessageBuilder.getAnswerMessage(sscsCaseDetails)
        );

        return cohEventActionContext;
    }

    @Override
    public String cohEvent() {
        return "answers_submitted";
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_ANSWERS_SUBMITTED;
    }

    @Override
    public boolean notifyAppellant() {
        return false;
    }
}
