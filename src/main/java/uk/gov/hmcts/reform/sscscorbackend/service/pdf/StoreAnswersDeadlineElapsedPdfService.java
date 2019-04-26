package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Service
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StoreAnswersDeadlineElapsedPdfService extends AbstractQuestionPdfService {

    @SuppressWarnings("squid:S00107")
    public StoreAnswersDeadlineElapsedPdfService(
            @Qualifier("oldPdfService") PdfService pdfService,
            @Value("${answer.html.template.path}") String templatePath,
            CcdPdfService ccdPdfService,
            IdamService idamService,
            QuestionService questionService,
            EvidenceManagementService evidenceManagementService) {
        super(pdfService, templatePath, ccdPdfService, idamService, questionService, evidenceManagementService);
    }

    @Override
    String documentNamePrefix() {
        return "Issued Answers Deadline Elapsed Round ";
    }
}
