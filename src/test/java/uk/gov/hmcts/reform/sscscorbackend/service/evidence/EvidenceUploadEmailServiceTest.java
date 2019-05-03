package uk.gov.hmcts.reform.sscscorbackend.service.evidence;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence.pdf;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence;

public class EvidenceUploadEmailServiceTest {

    private CorEmailService corEmailService;
    private SscsCaseDetails sscsCaseDetails;
    private String caseReference;
    private EmailMessageBuilder emailMessageBuilder;
    private FileDownloader fileDownloader;
    private UploadedEvidence evidenceDescriptionPdf;

    @Before
    public void setUp() {
        corEmailService = mock(CorEmailService.class);
        caseReference = "caseReference";
        sscsCaseDetails = SscsCaseDetails.builder().data(SscsCaseData.builder().caseReference(caseReference).build()).build();
        emailMessageBuilder = mock(EmailMessageBuilder.class);
        fileDownloader = mock(FileDownloader.class);
        evidenceDescriptionPdf = pdf(new byte[]{2, 4, 6, 0, 1}, "Evidence description");
    }

    @Test
    public void sendsEvidenceToDwp() {
        String message = "some message";
        String docUrl = "docUrl";
        String fileName = "fileName";
        SscsDocument sscsDocument = SscsDocument.builder()
                .value(SscsDocumentDetails.builder()
                        .documentFileName(fileName)
                        .documentLink(DocumentLink.builder()
                                .documentBinaryUrl(docUrl)
                                .build())
                        .build())
                .build();

        when(emailMessageBuilder.getEvidenceSubmittedMessage(sscsCaseDetails)).thenReturn(message);
        UploadedEvidence expectedFile = mock(UploadedEvidence.class);
        when(fileDownloader.downloadFile(docUrl)).thenReturn(expectedFile);

        new EvidenceUploadEmailService(corEmailService, emailMessageBuilder, fileDownloader).sendToDwp(
                evidenceDescriptionPdf,
                singletonList(sscsDocument),
                sscsCaseDetails
        );

        verify(corEmailService, times(1)).sendFileToDwp(
                evidenceDescriptionPdf,
                "Evidence uploaded (" + caseReference + ")",
                message
        );

        verify(fileDownloader).downloadFile(docUrl);
        verify(corEmailService, times(1)).sendFileToDwp(
                eq(expectedFile),
                eq("Evidence uploaded (" + caseReference + ")"),
                eq(message)
        );
    }
}