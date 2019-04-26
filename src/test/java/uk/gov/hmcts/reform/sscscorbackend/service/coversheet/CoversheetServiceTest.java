package uk.gov.hmcts.reform.sscscorbackend.service.coversheet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

public class CoversheetServiceTest {

    private String onlineHearingId;
    private OnlineHearingService onlineHearingService;
    private PdfService pdfService;
    private String template;

    @Before
    public void setUp() {
        onlineHearingId = "onlineHearingId";
        onlineHearingService = mock(OnlineHearingService.class);
        pdfService = mock(PdfService.class);
        template = "template";
    }

    @Test
    public void canLoadCcdCaseAndProducePdf() {
        when(onlineHearingService.getCcdCase(onlineHearingId))
                .thenReturn(Optional.of(createSscsCaseDetails()));

        byte[] pdf = {2, 4, 6, 0, 1};
        PdfCoverSheet pdfSummary = new PdfCoverSheet("12345", "line1", "line2", "town", "county", "postcode");
        when(pdfService.createPdf(pdfSummary, template)).thenReturn(pdf);

        Optional<byte[]> pdfOptional =
                new CoversheetService(onlineHearingService, pdfService, template).createCoverSheet(onlineHearingId);

        assertThat(pdfOptional.isPresent(), is(true));
        assertThat(pdfOptional.get(), is(pdf));
    }

    @Test
    public void cannotLoadCcdCase() {
        when(onlineHearingService.getCcdCase(onlineHearingId)).thenReturn(Optional.empty());

        Optional<byte[]> pdfOptional =
                new CoversheetService(onlineHearingService, pdfService, template).createCoverSheet(onlineHearingId);

        assertThat(pdfOptional.isPresent(), is(false));
    }

    private SscsCaseDetails createSscsCaseDetails() {
        return SscsCaseDetails.builder()
                .id(12345L)
                .data(SscsCaseData.builder()
                        .appeal(Appeal.builder()
                                .appellant(Appellant.builder()
                                        .address(Address.builder()
                                                .line1("line1")
                                                .line2("line2")
                                                .town("town")
                                                .county("county")
                                                .postcode("postcode")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
    }
}