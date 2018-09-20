package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohOnlineHearingId;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.CcdClient;
import uk.gov.hmcts.reform.sscs.ccd.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohOnlineHearings;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;

public class OnlineHearingServiceTest {
    private CohService cohService;
    private CcdClient ccdClient;

    private OnlineHearingService underTest;

    private String someEmailAddress;
    private Long someCaseId;
    private CcdRequestDetails ccdRequestDetails;


    @Before
    public void setUp() {
        cohService = mock(CohService.class);
        ccdClient = mock(CcdClient.class);
        ccdRequestDetails = CcdRequestDetails.builder().build();
        underTest = new OnlineHearingService(cohService, ccdClient, ccdRequestDetails);

        someEmailAddress = "someEmailAddress";
        someCaseId = 1234321L;
    }

    @Test
    public void createOnlineHearing() {
        String hearingId = "hearingId";
        when(cohService.createOnlineHearing(someRequest())).thenReturn("hearingId");

        String createdHearingId = underTest.createOnlineHearing(someRequest().getCaseId());

        assertThat(createdHearingId, is(hearingId));
    }

    @Test
    public void getsAnOnlineHearing() {
        String expectedCaseReference = "someCaseReference";
        String firstName = "firstName";
        String lastName = "lastName";

        SscsCaseDetails caseDetails = createCaseDetails(someCaseId, expectedCaseReference, firstName, lastName);
        when(ccdClient.findCaseBy(ccdRequestDetails, singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress)))
                .thenReturn(Arrays.asList(
                        createNonOnlineHearingCaseDetails(22222L, "otherCaseRef", "otherFirstName", "otherLastName"),
                        caseDetails));

        CohOnlineHearings cohOnlineHearings = someCohOnlineHearingId();
        when(cohService.getOnlineHearing(someCaseId)).thenReturn(cohOnlineHearings);

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);

        assertThat(onlineHearing.isPresent(), is(true));
        String expectedOnlineHearingId = cohOnlineHearings.getOnlineHearings().get(0).getOnlineHearingId();
        assertThat(onlineHearing.get().getOnlineHearingId(), is(expectedOnlineHearingId));
        assertThat(onlineHearing.get().getCaseReference(), is(expectedCaseReference));
        assertThat(onlineHearing.get().getAppellantName(), is(firstName + " " + lastName));
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionWhenGettingAHearingIfThereIsMoreThanOneCaseWithAnOnlinePanel() {
        SscsCaseDetails caseDetails1 = createCaseDetails(someCaseId, "someCaseReference", "firstName", "lastName");
        SscsCaseDetails caseDetails2 = createCaseDetails(22222L, "otherRef", "otherFirstName", "otherLatName");
        when(ccdClient.findCaseBy(ccdRequestDetails, singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress)))
                .thenReturn(Arrays.asList(
                        caseDetails1,
                        caseDetails2));

        underTest.getOnlineHearing(someEmailAddress);
    }

    @Test
    public void noOnlineHearingIfNotFoundInCcd() {
        when(ccdClient.findCaseBy(ccdRequestDetails, singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress)))
                .thenReturn(singletonList(createCaseDetails(someCaseId, "caseref", "firstname", "lastname")));

        CohOnlineHearings emptyCohOnlineHearings = new CohOnlineHearings(Collections.emptyList());
        when(cohService.getOnlineHearing(someCaseId)).thenReturn(emptyCohOnlineHearings);

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);
        assertThat(onlineHearing.isPresent(), is(false));
    }

    @Test
    public void noOnlineHearingIfNotFoundInCOh() {
        when(ccdClient.findCaseBy(ccdRequestDetails, singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress)))
                .thenReturn(emptyList());

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);
        assertThat(onlineHearing.isPresent(), is(false));
    }

    private SscsCaseDetails createCaseDetails(Long caseId, String expectedCaseReference, String firstName, String lastName) {
        return SscsCaseDetails.builder()
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

    private SscsCaseDetails createNonOnlineHearingCaseDetails(Long caseId, String expectedCaseReference, String firstName, String lastName) {
        return SscsCaseDetails.builder()
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
