package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohOnlineHearings;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.Panel;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.CcdClient;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.CcdRequestDeatils;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRequest;

public class OnlineHearingServiceTest {
    private CohClient cohClient;
    private CcdClient ccdClient;

    private OnlineHearingService underTest;

    private String someEmailAddress;
    private Long someCaseId;
    private CcdRequestDeatils ccdRequestDeatils;


    @Before
    public void setUp() {
        cohClient = mock(CohClient.class);
        ccdClient = mock(CcdClient.class);
        ccdRequestDeatils = CcdRequestDeatils.builder().build();
        underTest = new OnlineHearingService(cohClient, ccdClient, ccdRequestDeatils);

        someEmailAddress = "someEmailAddress";
        someCaseId = 1234321L;
    }

    @Test
    public void createOnlineHearing() {
        String hearingId = "hearingId";
        when(cohClient.createOnlineHearing(someRequest())).thenReturn("hearingId");

        String createdHearingId = underTest.createOnlineHearing(someRequest().getCaseId(), somePanel());

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

        List<PanelRequest> panelRequestList = underTest.convertPanel(ccdPanel);

        assertThat(panelRequestList.size(), is(3));

        //assumes the order for now
        assertThat(judgeName, is(panelRequestList.get(0).getName()));

        assertThat(medicalMemberName, is(panelRequestList.get(1).getName()));

        assertThat(disabilityQualifiedMemberName, is(panelRequestList.get(2).getName()));
    }

    @Test
    public void testConvertPanelNull() {
        Panel ccdPanel = null;

        List<PanelRequest> panelRequestList = underTest.convertPanel(ccdPanel);

        assertThat(panelRequestList.size(), is(0));
    }

    @Test
    public void getsAnOnlineHearing() {
        String expectedCaseReference = "someCaseReference";
        String firstName = "firstName";
        String lastName = "lastName";

        CaseDetails caseDetails = createCaseDetails(someCaseId, expectedCaseReference, firstName, lastName);
        when(ccdClient.findCaseBy(ccdRequestDeatils, singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress)))
                .thenReturn(Arrays.asList(
                        createNonOnlineHearingCaseDetails(22222L, "otherCaseRef", "otherFirstName", "otherLastName"),
                        caseDetails));

        CohOnlineHearings cohOnlineHearings = someCohOnlineHearingId();
        when(cohClient.getOnlineHearing(someCaseId)).thenReturn(cohOnlineHearings);

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);

        assertThat(onlineHearing.isPresent(), is(true));
        String expectedOnlineHearingId = cohOnlineHearings.getOnlineHearings().get(0).getOnlineHearingId();
        assertThat(onlineHearing.get().getOnlineHearingId(), is(expectedOnlineHearingId));
        assertThat(onlineHearing.get().getCaseReference(), is(expectedCaseReference));
        assertThat(onlineHearing.get().getAppellantName(), is(firstName + " " + lastName));
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionWhenGettingAHearingIfThereIsMoreThanOneCaseWithAnOnlinePanel() {
        CaseDetails caseDetails1 = createCaseDetails(someCaseId, "someCaseReference", "firstName", "lastName");
        CaseDetails caseDetails2 = createCaseDetails(22222L, "otherRef", "otherFirstName", "otherLatName");
        when(ccdClient.findCaseBy(ccdRequestDeatils, singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress)))
                .thenReturn(Arrays.asList(
                        caseDetails1,
                        caseDetails2));

        underTest.getOnlineHearing(someEmailAddress);
    }

    @Test
    public void noOnlineHearingIfNotFoundInCcd() {
        when(ccdClient.findCaseBy(ccdRequestDeatils, singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress)))
                .thenReturn(singletonList(createCaseDetails(someCaseId, "caseref", "firstname", "lastname")));

        CohOnlineHearings emptyCohOnlineHearings = new CohOnlineHearings(Collections.emptyList());
        when(cohClient.getOnlineHearing(someCaseId)).thenReturn(emptyCohOnlineHearings);

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);
        assertThat(onlineHearing.isPresent(), is(false));
    }

    @Test
    public void noOnlineHearingIfNotFoundInCOh() {
        when(ccdClient.findCaseBy(ccdRequestDeatils, singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress)))
                .thenReturn(emptyList());

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);
        assertThat(onlineHearing.isPresent(), is(false));
    }

    private CaseDetails createCaseDetails(Long caseId, String expectedCaseReference, String firstName, String lastName) {
        return CaseDetails.builder()
                .id(caseId)
                .data(SscsCaseData.builder()
                        .caseReference(expectedCaseReference)
                        .onlinePanel(OnlinePanel.builder().assignedTo("someJudge").build())
                        .appeal(Appeal.builder()
                                .appellant(Appellant.builder()
                                        .name(Name.builder()
                                                .firstName(firstName)
                                                .lastName(lastName)
                                                .build()
                                        ).build()
                                ).build()
                        ).build()

                ).build();
    }

    private CaseDetails createNonOnlineHearingCaseDetails(Long caseId, String expectedCaseReference, String firstName, String lastName) {
        return CaseDetails.builder()
                .id(caseId)
                .data(SscsCaseData.builder()
                        .caseReference(expectedCaseReference)
                        .appeal(Appeal.builder()
                                .appellant(Appellant.builder()
                                        .name(Name.builder()
                                                .firstName(firstName)
                                                .lastName(lastName)
                                                .build()
                                        ).build()
                                ).build()
                        ).build()

                ).build();
    }
}
