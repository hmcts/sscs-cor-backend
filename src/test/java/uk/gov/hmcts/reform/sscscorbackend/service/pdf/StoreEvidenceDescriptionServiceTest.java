package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.EvidenceDescription;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfEvidenceDescription;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.EvidenceDescriptionPdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.OldPdfService;

public class StoreEvidenceDescriptionServiceTest {
    private StoreEvidenceDescriptionService storeEvidenceDescriptionService;

    @Before
    public void setUp() throws Exception {
        storeEvidenceDescriptionService = new StoreEvidenceDescriptionService(
                mock(OldPdfService.class),
                "template_path",
                mock(CcdPdfService.class),
                mock(IdamService.class),
                mock(EvidenceManagementService.class)
        );
    }

    @Test
    public void pdfHasNotAlreadyBeenCreatedIsAlwaysTrue() {
        assertTrue(storeEvidenceDescriptionService.pdfHasNotAlreadyBeenCreated(mock(SscsCaseDetails.class),
            "docPrefix"));
    }

    @Test
    public void documentNamePrefixIsEvidenceDescription() {
        assertThat(storeEvidenceDescriptionService
            .documentNamePrefix(mock(SscsCaseDetails.class), "hearingId", null),
            is("temporal unique Id ec7ae162-9834-46b7-826d-fdc9935e3187 Evidence Description - "));
    }

    @Test
    public void canGetPdfContent() {
        PdfAppealDetails appealDetails = mock(PdfAppealDetails.class);
        List<String> fileName = Collections.singletonList("fileName");
        String description = "description";
        EvidenceDescriptionPdfData evidenceDescriptionPdfData = new EvidenceDescriptionPdfData(
            mock(SscsCaseDetails.class), new EvidenceDescription(description, "idamEmail"), fileName);
        PdfEvidenceDescription pdfContent = storeEvidenceDescriptionService.getPdfContent(evidenceDescriptionPdfData,
            "hearingId", appealDetails);

        assertThat(pdfContent, is(new PdfEvidenceDescription(appealDetails, description, fileName)));
    }
}