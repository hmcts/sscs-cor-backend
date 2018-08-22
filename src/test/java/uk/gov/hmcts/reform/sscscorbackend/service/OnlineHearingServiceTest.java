package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohOnlineHearingId;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.somePanel;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohOnlineHearings;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.Panel;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.CcdClient;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.domain.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRequest;

public class OnlineHearingServiceTest {
    private CohClient cohClient;
    private CcdClient ccdClient;

    private OnlineHearingService underTest;

    private String someEmailAddress;
    private Long someCaseId;


    @Before
    public void setUp() {
        cohClient = mock(CohClient.class);
        ccdClient = mock(CcdClient.class);
        underTest = new OnlineHearingService(cohClient, ccdClient);

        someEmailAddress = "someEmailAddress";
        someCaseId = 1234321L;
    }

    @Test
    public void createOnlineHearing() {
        String hearingId = "hearingId";
        when(cohClient.createOnlineHearing(someRequest())).thenReturn("hearingId");

        String createdHearingId = new OnlineHearingService(cohClient, ccdClient).createOnlineHearing(someRequest().getCaseId(), somePanel());

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
                new OnlineHearingService(cohClient, ccdClient);

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
                new OnlineHearingService(cohClient, ccdClient);

        List<PanelRequest> panelRequestList = onlineHearingService.convertPanel(ccdPanel);

        assertThat(panelRequestList.size(), is(0));
    }

    @Test
    public void getsAnOnlineHearing() {
        CohOnlineHearings cohOnlineHearings = someCohOnlineHearingId();
        when(ccdClient.findCaseBy(singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress)))
                .thenReturn(singletonList(CaseDetails.builder().id(someCaseId).build()));
        when(cohClient.getOnlineHearing(someCaseId)).thenReturn(cohOnlineHearings);

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);

        Assert.assertThat(onlineHearing.isPresent(), is(true));
        String expectedOnlineHearingId = cohOnlineHearings.getOnlineHearings().get(0).getOnlineHearingId();
        Assert.assertThat(onlineHearing.get().getOnlineHearingId(), is(expectedOnlineHearingId));
    }

    @Test
    public void noOnlineHearingIfNotFoundInCOh() {
        when(ccdClient.findCaseBy(singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress)))
                .thenReturn(singletonList(CaseDetails.builder().id(someCaseId).build()));

        CohOnlineHearings emptyCohOnlineHearings = new CohOnlineHearings(Collections.emptyList());
        when(cohClient.getOnlineHearing(someCaseId)).thenReturn(emptyCohOnlineHearings);

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);
        Assert.assertThat(onlineHearing.isPresent(), is(false));
    }
}
