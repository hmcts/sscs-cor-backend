package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

public class HearingRelistedActionTest {

    private StoreOnlineHearingService storeOnlineHearingService;
    private NotificationsService notificationsService;
    private CorCcdService corCcdService;
    private IdamService idamService;
    private IdamTokens idamTokens;
    private Long caseId;
    private String onlineHearingId;
    private CohEvent cohEvent;
    private HearingRelistedAction underTest;

    @Before
    public void setUp() {
        storeOnlineHearingService = mock(StoreOnlineHearingService.class);
        notificationsService = mock(NotificationsService.class);
        corCcdService = mock(CorCcdService.class);
        idamService = mock(IdamService.class);
        idamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        caseId = 12345L;
        onlineHearingId = "onlineHearingId";
        cohEvent = someCohEvent(caseId.toString(), onlineHearingId, "continuous_online_hearing_relisted");

        underTest = new HearingRelistedAction(storeOnlineHearingService, notificationsService, corCcdService, idamService);
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
        when(corCcdService.getByCaseId(caseId, idamTokens)).thenReturn(sscsCaseDetails);

        underTest.handle(caseId, onlineHearingId, cohEvent);

        verify(storeOnlineHearingService).storePdf(caseId, onlineHearingId);
        verify(notificationsService).send(cohEvent);
        ArgumentMatcher<SscsCaseData> hasOralHearing = data -> data.getAppeal().getHearingType().equals("oral");
        verify(corCcdService).updateCase(
                argThat(hasOralHearing),
                eq(caseId),
                eq("updateHearingType"),
                eq("SSCS - appeal updated event"),
                eq("Update SSCS hearing type"),
                eq(idamTokens));
    }
}