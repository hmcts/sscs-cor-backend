package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.BasePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreQuestionsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

@Service
public class QuestionRoundIssuedEventAction implements CohEventAction {
    private final StoreQuestionsPdfService storeQuestionsPdfService;
    private final CorEmailService corEmailService;
    private final DwpEmailMessageBuilder dwpEmailMessageBuilder;

    public QuestionRoundIssuedEventAction(StoreQuestionsPdfService storeQuestionsPdfService,
                                          CorEmailService corEmailService,
                                          DwpEmailMessageBuilder dwpEmailMessageBuilder) {
        this.storeQuestionsPdfService = storeQuestionsPdfService;
        this.corEmailService = corEmailService;
        this.dwpEmailMessageBuilder = dwpEmailMessageBuilder;
    }

    @Override
    public void handle(Long caseId, String onlineHearingId, StorePdfResult storePdfResult) {
        SscsCaseDetails sscsCaseDetails = storePdfResult.getDocument();
        sendDwpEmail(storePdfResult, sscsCaseDetails);
    }

    public BasePdfService getPdfService() {
        return storeQuestionsPdfService;
    }

    private void sendDwpEmail(StorePdfResult storePdfResult, SscsCaseDetails sscsCaseDetails) {
        corEmailService.sendPdf(
                storePdfResult,
                getDwpEmailSubject(sscsCaseDetails),
                getDwpEmailMessage(sscsCaseDetails)
        );
    }

    private String getDwpEmailMessage(SscsCaseDetails sscsCaseDetails) {
        return dwpEmailMessageBuilder.getQuestionMessage(sscsCaseDetails);
    }

    private String getDwpEmailSubject(SscsCaseDetails sscsCaseDetails) {
        return "Questions issued to the appellant (" + sscsCaseDetails.getData().getCaseReference() + ")";
    }

    @Override
    public String eventCanHandle() {
        return "question_round_issued";
    }
}
