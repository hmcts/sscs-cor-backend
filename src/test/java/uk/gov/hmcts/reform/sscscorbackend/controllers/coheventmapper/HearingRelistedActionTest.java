package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.Pdf;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;

public class HearingRelistedActionTest {

    private CorCcdService corCcdService;
    private IdamTokens idamTokens;
    private Long caseId;
    private String onlineHearingId;
    private HearingRelistedAction underTest;
    private CorEmailService corEmailService;
    private DwpEmailMessageBuilder dwpEmailMessageBuilder;

    @Before
    public void setUp() {
        corCcdService = mock(CorCcdService.class);
        IdamService idamService = mock(IdamService.class);
        idamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        caseId = 12345L;
        onlineHearingId = "onlineHearingId";
        corEmailService = mock(CorEmailService.class);

        dwpEmailMessageBuilder = mock(DwpEmailMessageBuilder.class);
        underTest = new HearingRelistedAction(mock(StoreOnlineHearingService.class), corCcdService, idamService, corEmailService, dwpEmailMessageBuilder);
    }

    @Test
    public void handlesActions() {
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
                        .appeal(Appeal.builder()
                                .hearingType("cor")
                                .build())
                        .build())
                .build();
        StorePdfResult storePdfResult = new StorePdfResult(mock(Pdf.class), sscsCaseDetails);
        when(dwpEmailMessageBuilder.getRelistedMessage(sscsCaseDetails)).thenReturn("message body");

        underTest.handle(caseId, onlineHearingId, storePdfResult);

        ArgumentMatcher<SscsCaseData> hasOralHearing = data -> data.getAppeal().getHearingType().equals("oral");
        verify(corCcdService).updateCase(
                argThat(hasOralHearing),
                eq(caseId),
                eq("updateHearingType"),
                eq("SSCS - appeal updated event"),
                eq("Update SSCS hearing type"),
                eq(idamTokens));
        verify(corEmailService).sendEmail("COR: Hearing required", "message body");
    }
}