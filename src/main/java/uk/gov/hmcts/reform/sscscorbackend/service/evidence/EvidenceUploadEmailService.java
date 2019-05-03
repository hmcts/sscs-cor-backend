package uk.gov.hmcts.reform.sscscorbackend.service.evidence;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocumentDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence;

@Service
public class EvidenceUploadEmailService {
    private final CorEmailService corEmailService;
    private final EmailMessageBuilder emailMessageBuilder;
    private final FileDownloader fileDownloader;

    public EvidenceUploadEmailService(CorEmailService corEmailService, EmailMessageBuilder emailMessageBuilder, FileDownloader fileDownloader) {
        this.corEmailService = corEmailService;
        this.emailMessageBuilder = emailMessageBuilder;
        this.fileDownloader = fileDownloader;
    }

    public void sendToDwp(UploadedEvidence evidenceDescription, List<SscsDocument> newEvidence, SscsCaseDetails sscsCaseDetails) {
        String message = emailMessageBuilder.getEvidenceSubmittedMessage(sscsCaseDetails);
        String subject = "Evidence uploaded (" + sscsCaseDetails.getData().getCaseReference() + ")";

        corEmailService.sendFileToDwp(evidenceDescription, subject, message);

        newEvidence.forEach(evidence -> {
            SscsDocumentDetails evidenceValue = evidence.getValue();
            String documentUrl = evidenceValue.getDocumentLink().getDocumentBinaryUrl();

            corEmailService.sendFileToDwp(
                    fileDownloader.downloadFile(documentUrl),
                    subject,
                    message
            );
        });
    }

}
