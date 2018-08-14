package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.somePanel;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someRequest;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.Panel;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRequest;

public class OnlineHearingServiceTest {
    private CohClient cohClient;

    @Before
    public void setUp() {
        cohClient = mock(CohClient.class);
    }

    @Test
    public void createOnlineHearing() {
        String hearingId = "hearingId";
        when(cohClient.createOnlineHearing(someRequest())).thenReturn("hearingId");

        String createdHearingId = new OnlineHearingService(cohClient).createOnlineHearing(someRequest().getCaseId(), somePanel());

        assertThat(createdHearingId, is(hearingId));
    }

    @Test
    public void testConvertPanel() {
        String judgeName = "Judge Paul Baker";
        String medicalMemberName = "Doctor Janet Wren";
        String disabilityQualifiedMemberName = "Miss Emily Smith";

        Panel ccdPanel = new Panel(judgeName,
                medicalMemberName,
                disabilityQualifiedMemberName);

        OnlineHearingService onlineHearingService =
                new OnlineHearingService(cohClient);

        List<PanelRequest> panelRequestList = onlineHearingService.convertPanel(ccdPanel);

        assertThat(panelRequestList.size(), is(3));

        //assumes the order for now
        assertThat(judgeName, is(panelRequestList.get(0).getName()));

        assertThat(medicalMemberName, is(panelRequestList.get(1).getName()));

        assertThat(disabilityQualifiedMemberName, is(panelRequestList.get(2).getName()));
    }

    @Test
    public void testConvertPanelNull() {
        Panel ccdPanel = null;

        OnlineHearingService onlineHearingService =
                new OnlineHearingService(cohClient);

        List<PanelRequest> panelRequestList = onlineHearingService.convertPanel(ccdPanel);

        assertThat(panelRequestList.size(), is(0));
    }
}
