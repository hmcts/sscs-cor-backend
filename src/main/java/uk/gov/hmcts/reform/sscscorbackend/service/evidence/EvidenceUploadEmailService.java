package uk.gov.hmcts.reform.sscscorbackend.service.evidence;

import static java.util.stream.Collectors.toList;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.CorDocument;
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

        sendFileToDwp(
                newEvidence.stream().map(SscsDocument::getValue).collect(toList()),
                message,
                subject
        );
    }

    public void sendToDwp(String questionSubject, List<CorDocument> newEvidence, SscsCaseDetails sscsCaseDetails) {
        String message = emailMessageBuilder.getQuestionEvidenceSubmittedMessage(sscsCaseDetails, questionSubject);
        String subject = "Evidence uploaded (" + sscsCaseDetails.getData().getCaseReference() + ")";

        sendFileToDwp(
                newEvidence.stream().map(evidence -> evidence.getValue().getDocument()).collect(toList()),
                message,
                subject
        );
    }

    private void sendFileToDwp(List<SscsDocumentDetails> newEvidence, String message, String subject) {
        newEvidence.forEach(evidence -> {
            String documentUrl = evidence.getDocumentLink().getDocumentBinaryUrl();

            corEmailService.sendFileToDwp(
                    fileDownloader.downloadFile(documentUrl),
                    subject,
                    message
            );
        });
    }

}
