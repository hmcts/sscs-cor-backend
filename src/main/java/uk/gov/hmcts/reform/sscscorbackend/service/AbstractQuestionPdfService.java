package uk.gov.hmcts.reform.sscscorbackend.service;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

public abstract class AbstractQuestionPdfService extends StorePdfService<PdfQuestionsSummary> {
    private final QuestionService questionService;

    @SuppressWarnings("squid:S00107")
    public AbstractQuestionPdfService(
            PdfService pdfService,
            String templatePath,
            SscsPdfService sscsPdfService,
            IdamService idamService,
            QuestionService questionService,
            EvidenceManagementService evidenceManagementService) {
        super(pdfService, templatePath, sscsPdfService, idamService, evidenceManagementService);
        this.questionService = questionService;
    }

    @Override
    protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId) {
        int currentQuestionRound = questionService.getCurrentQuestionRound(onlineHearingId);
        return documentNamePrefix() + currentQuestionRound + " - ";
    }

    abstract String documentNamePrefix();

    @Override
    protected PdfQuestionsSummary getPdfContent(SscsCaseDetails caseDetails, String onlineHearingId, PdfAppealDetails appealDetails) {
        QuestionRound questions = questionService.getQuestions(onlineHearingId);
        return new PdfQuestionsSummary(appealDetails, questions.getQuestions());
    }
}
