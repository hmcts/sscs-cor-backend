package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreAnswersPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

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
    public void handle(Long caseId, String onlineHearingId, StorePdfResult storePdfResult) {
        SscsCaseDetails sscsCaseDetails = storePdfResult.getDocument();
        String caseReference = sscsCaseDetails.getData().getCaseReference();
        corEmailService.sendPdfToDwp(
                storePdfResult,
                "Appellant has provided information (" + caseReference + ")",
                dwpEmailMessageBuilder.getAnswerMessage(sscsCaseDetails)
        );
    }

    @Override
    public StorePdfService getPdfService() {
        return storeAnswersPdfService;
    }

    @Override
    public String eventCanHandle() {
        return "answers_submitted";
    }

    @Override
    public boolean notifyAppellant() {
        return false;
    }
}
