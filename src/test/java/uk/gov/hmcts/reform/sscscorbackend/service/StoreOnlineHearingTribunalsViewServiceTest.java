package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

public class StoreOnlineHearingTribunalsViewServiceTest {

    private final String someCaseReference = "CaseReference";
    private final Long someCaseId = 123456L;
    private OnlineHearingService onlineHearingService;
    private OnlineHearingDateReformatter onlineHearingDateReformatter;
    private PdfService pdfService;
    private StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService;
    private SscsPdfService sscsPdfService;
    private CcdService ccdService;
    private IdamTokens idamTokens;
    private SscsCaseDetails sscsCaseDetails;
    private SscsCaseData sscsCaseData;
    private String someHearingId;

    @Before
    public void setUp() {
        onlineHearingService = mock(OnlineHearingService.class);
        onlineHearingDateReformatter = mock(OnlineHearingDateReformatter.class);
        pdfService = mock(PdfService.class);
        sscsPdfService = mock(SscsPdfService.class);
        sscsPdfService = mock(SscsPdfService.class);
        IdamService idamService = mock(IdamService.class);
        ccdService = mock(CcdService.class);
        idamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        storeOnlineHearingTribunalsViewService = new StoreOnlineHearingTribunalsViewService(
                onlineHearingService,
                pdfService,
                onlineHearingDateReformatter,
                sscsPdfService,
                ccdService,
                idamService,
                mock(EvidenceManagementService.class));
        someHearingId = "someHearingId";
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfCannotFindHearingId() {
        sscsCaseData = createSscsCaseData();
        sscsCaseDetails = SscsCaseDetails.builder().data(sscsCaseData).build();

        when(ccdService.getByCaseId(someCaseId, idamTokens)).thenReturn(sscsCaseDetails);

        when(onlineHearingService.loadOnlineHearingFromCoh(sscsCaseDetails)).thenReturn(Optional.empty());
        storeOnlineHearingTribunalsViewService.storePdf(someCaseId, someHearingId);
    }

    private SscsCaseData createSscsCaseData() {
        return SscsCaseData.builder()
                .caseReference(someCaseReference)
                .appeal(
                        Appeal.builder().appellant(Appellant.builder()
                                .name(Name.builder()
                                        .title("Mr")
                                    .firstName("Jean")
                                    .lastName("Valjean")
                                    .build())
                                .identity(Identity.builder()
                                        .nino("someNino")
                                        .build())
                            .build())
                        .build())
                .build();
    }
}