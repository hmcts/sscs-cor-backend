package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

public class StoreOnlineHearingTribunalsViewServiceTest {

    private final String someCaseReference = "CaseReference";
    private final Long someCaseId = 123456L;
    private OnlineHearingService onlineHearingService;
    private OnlineHearingDateReformatter onlineHearingDateReformatter;
    private PdfService pdfService;
    private StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService;
    private SscsPdfService sscsPdfService;
    private IdamTokens idamTokens;
    private SscsCaseDetails sscsCaseDetails;
    private SscsCaseData sscsCaseData;
    private String someHearingId;
    private ActivitiesValidator activitiesValidator;

    @Before
    public void setUp() throws IOException {
        onlineHearingService = mock(OnlineHearingService.class);
        onlineHearingDateReformatter = mock(OnlineHearingDateReformatter.class);
        pdfService = mock(PdfService.class);
        sscsPdfService = mock(SscsPdfService.class);
        sscsPdfService = mock(SscsPdfService.class);
        IdamService idamService = mock(IdamService.class);
        idamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        activitiesValidator = mock(ActivitiesValidator.class);
        storeOnlineHearingTribunalsViewService = new StoreOnlineHearingTribunalsViewService(
                onlineHearingService,
                pdfService,
                "sometemplate",
                onlineHearingDateReformatter,
                sscsPdfService,
                idamService,
                mock(EvidenceManagementService.class),
                activitiesValidator);
        someHearingId = "someHearingId";
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfCannotFindHearingId() {
        sscsCaseData = createSscsCaseData();
        sscsCaseDetails = SscsCaseDetails.builder().data(sscsCaseData).build();

        when(onlineHearingService.loadOnlineHearingFromCoh(sscsCaseDetails)).thenReturn(Optional.empty());
        storeOnlineHearingTribunalsViewService.storePdf(someCaseId, someHearingId, sscsCaseDetails);
    }

    @Test
    public void getPdfContentVerifiesContent() {
        SscsCaseDetails caseDetails = mock(SscsCaseDetails.class);
        OnlineHearing onlineHearing = mock(OnlineHearing.class);
        when(onlineHearingService.loadOnlineHearingFromCoh(caseDetails)).thenReturn(Optional.of(onlineHearing));

        storeOnlineHearingTribunalsViewService.getPdfContent(caseDetails, "hearingId", mock(PdfAppealDetails.class));

        verify(activitiesValidator).validateWeHaveMappingForActivities(onlineHearing);
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