package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Service
public class StoreQuestionsPdfService extends BasePdfService<PdfQuestionsSummary> {
    private final QuestionService questionService;

    public StoreQuestionsPdfService(
            @Qualifier("QuestionPdfService") PdfService pdfService,
            SscsPdfService sscsPdfService,
            CcdService ccdService,
            IdamService idamService,
            QuestionService questionService,
            EvidenceManagementService evidenceManagementService) {
        super(pdfService, sscsPdfService, ccdService, idamService, evidenceManagementService);
        this.questionService = questionService;
    }

    @Override
    protected String documentNameStartsWith() {
        return "Issued Questions - ";
    }

    @Override
    protected PdfQuestionsSummary getPdfContent(SscsCaseDetails caseDetails, String onlineHearingId, PdfAppealDetails appealDetails) {
        QuestionRound questions = questionService.getQuestions(onlineHearingId);
        return new PdfQuestionsSummary(appealDetails, questions.getQuestions());
    }
}
