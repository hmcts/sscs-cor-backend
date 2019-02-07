package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Service
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StoreAnswersPdfService extends AbstractQuestionPdfService {

    @SuppressWarnings("squid:S00107")
    public StoreAnswersPdfService(
            PdfService pdfService,
            @Value("${answer.html.template.path}") String templatePath,
            SscsPdfService sscsPdfService,
            CcdService ccdService,
            IdamService idamService,
            QuestionService questionService,
            EvidenceManagementService evidenceManagementService) {
        super(pdfService, templatePath, sscsPdfService, ccdService, idamService, questionService, evidenceManagementService);
    }

    @Override
    String documentNamePrefix() {
        return "Issued Answers Round ";
    }
}
