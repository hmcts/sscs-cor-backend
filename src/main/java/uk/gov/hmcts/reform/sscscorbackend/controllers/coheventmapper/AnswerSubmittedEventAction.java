package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.BasePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreAnswersPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

@Service
public class AnswerSubmittedEventAction implements CohEventAction {
    private final CorEmailService corEmailService;
    private final BasePdfService storeAnswersPdfService;
    private final DwpEmailMessageBuilder dwpEmailMessageBuilder;

    public AnswerSubmittedEventAction(CorEmailService corEmailService, StoreAnswersPdfService storeAnswersPdfService, DwpEmailMessageBuilder dwpEmailMessageBuilder) {
        this.corEmailService = corEmailService;
        this.storeAnswersPdfService = storeAnswersPdfService;
        this.dwpEmailMessageBuilder = dwpEmailMessageBuilder;
    }

    @Override
    public void handle(Long caseId, String onlineHearingId) {
        StorePdfResult storePdfResult = storeAnswersPdfService.storePdf(caseId, onlineHearingId);
        SscsCaseDetails sscsCaseDetails = storePdfResult.getDocument();
        String caseReference = sscsCaseDetails.getData().getCaseReference();
        corEmailService.sendPdf(
                storePdfResult,
                "Appellant has provided information (" + caseReference + ")",
                dwpEmailMessageBuilder.getAnswerMessage(sscsCaseDetails)
        );
    }

    @Override
    public boolean notifyAppellant() {
        return false;
    }
}
