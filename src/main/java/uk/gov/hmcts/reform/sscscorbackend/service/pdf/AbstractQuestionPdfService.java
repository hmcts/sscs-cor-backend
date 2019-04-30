package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import java.util.List;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionSummary;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfQuestionsSummary;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.util.QuestionUtils;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

public abstract class AbstractQuestionPdfService extends StorePdfService<PdfQuestionsSummary, PdfData> {
    private final QuestionService questionService;
    private final QuestionUtils questionUtils;

    @SuppressWarnings("squid:S00107")
    public AbstractQuestionPdfService(
            PdfService pdfService,
            String templatePath,
            CcdPdfService ccdPdfService,
            IdamService idamService,
            QuestionService questionService,
            EvidenceManagementService evidenceManagementService,
            QuestionUtils questionUtils) {
        super(pdfService, templatePath, ccdPdfService, idamService, evidenceManagementService);
        this.questionService = questionService;
        this.questionUtils = questionUtils;
    }

    @Override
    protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId) {
        int currentQuestionRound = questionService.getCurrentQuestionRound(onlineHearingId);
        return documentNamePrefix() + currentQuestionRound + " - ";
    }

    abstract String documentNamePrefix();

    @Override
    protected PdfQuestionsSummary getPdfContent(PdfData data, String onlineHearingId, PdfAppealDetails appealDetails) {
        QuestionRound questions = questionService.getQuestions(onlineHearingId, false);
        List<QuestionSummary> questionSummaries = questionUtils.cleanUpQuestionsHtml(questions.getQuestions());
        return new PdfQuestionsSummary(appealDetails, questionSummaries);
    }
}
