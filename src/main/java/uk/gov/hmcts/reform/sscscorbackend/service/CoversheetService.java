package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Address;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Service
public class CoversheetService {
    private final OnlineHearingService onlineHearingService;
    private final PdfService pdfService;
    private final String template;

    public CoversheetService(
            OnlineHearingService onlineHearingService,
            @Qualifier("docmosisPdfService") PdfService pdfService,
            @Value("${evidenceCoverSheet.docmosis.template}") String template) {
        this.onlineHearingService = onlineHearingService;
        this.pdfService = pdfService;
        this.template = template;
    }

    public Optional<byte[]> createCoverSheet(String onlineHearingId) {
        return onlineHearingService.getCcdCase(onlineHearingId)
                .map(sscsCase -> {
                    SscsCaseData sscsCaseData = sscsCase.getData();
                    Address address = sscsCaseData.getAppeal().getAppellant().getAddress();
                    PdfCoverSheet pdfCoverSheet = new PdfCoverSheet(
                            "" + sscsCase.getId(),
                            address.getLine1(),
                            address.getLine2(),
                            address.getTown(),
                            address.getCounty(),
                            address.getPostcode()
                    );

                    return pdfService.createPdf(pdfCoverSheet, template);
                });
    }
}
