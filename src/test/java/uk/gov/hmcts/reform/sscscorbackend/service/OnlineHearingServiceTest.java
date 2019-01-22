package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.*;

import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.domain.Decision;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.DecisionExtractor;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.*;

public class OnlineHearingServiceTest {
    private CohService cohService;
    private CorCcdService ccdService;
    private DecisionExtractor decisionExtractor;

    private OnlineHearingService underTest;

    private String someEmailAddress;
    private Long someCaseId;
    private IdamTokens idamTokens;
    private AmendPanelMembersService amendPanelMembersService;
    private IdamService idamService;

    @Before
    public void setUp() {
        cohService = mock(CohService.class);
        ccdService = mock(CorCcdService.class);
        decisionExtractor = mock(DecisionExtractor.class);
        idamTokens = IdamTokens.builder().build();
        idamService = mock(IdamService.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);

        amendPanelMembersService = mock(AmendPanelMembersService.class);
        underTest = new OnlineHearingService(cohService, ccdService, idamService, decisionExtractor, amendPanelMembersService, false);

        someEmailAddress = "someEmailAddress";
        someCaseId = 1234321L;
    }

    @Test
    public void createOnlineHearing() {
        CreateOnlineHearingRequest request = someRequest();
        CcdEvent ccdEvent = new CcdEvent(new CaseDetails(request.getCaseId(), null), new CaseDetails(request.getCaseId(), null));

        when(cohService.createOnlineHearing(request)).thenReturn(true);

        boolean createdOnlineHearing = underTest.createOnlineHearing(ccdEvent);

        assertThat(createdOnlineHearing, is(true));
        verify(amendPanelMembersService).amendPanelMembersPermissions(ccdEvent);
    }

    @Test
    public void getsAnOnlineHearing() {
        String expectedCaseReference = "someCaseReference";
        String firstName = "firstName";
        String lastName = "lastName";

        SscsCaseDetails caseDetails = createCaseDetails(someCaseId, expectedCaseReference, firstName, lastName);
        when(ccdService.findCaseBy(singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress), idamTokens))
                .thenReturn(asList(
                        createNonOnlineHearingCaseDetails(22222L, "otherCaseRef", "otherFirstName", "otherLastName"),
                        caseDetails));

        CohOnlineHearings cohOnlineHearings = someCohOnlineHearings();
        when(cohService.getOnlineHearing(someCaseId)).thenReturn(cohOnlineHearings);

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);

        assertThat(onlineHearing.isPresent(), is(true));
        String expectedOnlineHearingId = cohOnlineHearings.getOnlineHearings().get(0).getOnlineHearingId();
        assertThat(onlineHearing.get().getOnlineHearingId(), is(expectedOnlineHearingId));
        assertThat(onlineHearing.get().getCaseReference(), is(expectedCaseReference));
        assertThat(onlineHearing.get().getAppellantName(), is(firstName + " " + lastName));
    }

    @Test
    public void getsAnOnlineHearingWithCcdId() {
        underTest = new OnlineHearingService(cohService, ccdService, idamService, decisionExtractor, amendPanelMembersService, true);
        String expectedCaseReference = "someCaseReference";
        String firstName = "firstName";
        String lastName = "lastName";

        SscsCaseDetails caseDetails1 = createCaseDetails(someCaseId, expectedCaseReference, firstName, lastName);
        long someOtherCaseId = 88888L;
        SscsCaseDetails caseDetails2 = createCaseDetails(someOtherCaseId, "otherCaseRef", "otherFirstName", "otherLastName");
        when(ccdService.findCaseBy(singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress), idamTokens))
                .thenReturn(asList(
                        createNonOnlineHearingCaseDetails(22222L, "otherCaseRef", "otherFirstName", "otherLastName"),
                        caseDetails1,
                        caseDetails2));

        CohOnlineHearings cohOnlineHearings = someCohOnlineHearings();
        when(cohService.getOnlineHearing(someCaseId)).thenReturn(cohOnlineHearings);

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress + "+" + someCaseId);

        assertThat(onlineHearing.isPresent(), is(true));
        String expectedOnlineHearingId = cohOnlineHearings.getOnlineHearings().get(0).getOnlineHearingId();
        assertThat(onlineHearing.get().getOnlineHearingId(), is(expectedOnlineHearingId));
        assertThat(onlineHearing.get().getCaseReference(), is(expectedCaseReference));
        assertThat(onlineHearing.get().getAppellantName(), is(firstName + " " + lastName));
    }

    @Test
    public void getsAnOnlineHearingWithDecision() {
        String expectedCaseReference = "someCaseReference";
        String firstName = "firstName";
        String lastName = "lastName";

        SscsCaseDetails caseDetails = createCaseDetails(someCaseId, expectedCaseReference, firstName, lastName);
        when(ccdService.findCaseBy(singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress), idamTokens))
                .thenReturn(asList(
                        createNonOnlineHearingCaseDetails(22222L, "otherCaseRef", "otherFirstName", "otherLastName"),
                        caseDetails));

        CohOnlineHearings cohOnlineHearings = someCohOnlineHearings();
        when(cohService.getOnlineHearing(someCaseId)).thenReturn(cohOnlineHearings);

        String onlineHearingId = cohOnlineHearings.getOnlineHearings().get(0).getOnlineHearingId();
        CohDecision cohDecision = new CohDecision(
                onlineHearingId,
                "decisionAward",
                "decisionHeader",
                "decisionReason",
                "{}",
                new CohState("decision_state", "decision_date")
        );
        when(cohService.getDecision(onlineHearingId))
                .thenReturn(Optional.of(cohDecision));
        CohDecisionReply cohDecisionReply = new CohDecisionReply("reply", "replyReason", "replyDateTime", "authorRef");
        when(cohService.getDecisionReplies(onlineHearingId)).thenReturn(Optional.of(new CohDecisionReplies(singletonList(cohDecisionReply))));
        Decision someDecision = DataFixtures.someDecision();
        when(decisionExtractor.extract(someCaseId, cohDecision, cohDecisionReply)).thenReturn(someDecision);

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);

        assertThat(onlineHearing.isPresent(), is(true));
        Decision decision = onlineHearing.get().getDecision();
        assertThat(decision, is(someDecision));
    }

    @Test
    public void getsACcdIdFromAHearing() {
        String onlineHearingId = "someOnlineHearingId";
        CohOnlineHearing cohOnlineHearing = someCohOnlineHearing();
        when(cohService.getOnlineHearing(onlineHearingId)).thenReturn(cohOnlineHearing);

        Optional<Long> ccdCaseId = underTest.getCcdCaseId(onlineHearingId);

        assertThat(ccdCaseId.isPresent(), is(true));
        assertThat(ccdCaseId.get(), is(cohOnlineHearing.getCcdCaseId()));
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionWhenGettingAHearingIfThereIsMoreThanOneCaseWithAnOnlinePanel() {
        SscsCaseDetails caseDetails1 = createCaseDetails(someCaseId, "someCaseReference", "firstName", "lastName");
        SscsCaseDetails caseDetails2 = createCaseDetails(22222L, "otherRef", "otherFirstName", "otherLatName");
        when(ccdService.findCaseBy(singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress), idamTokens))
                .thenReturn(asList(caseDetails1, caseDetails2));

        underTest.getOnlineHearing(someEmailAddress);
    }

    @Test
    public void noOnlineHearingIfNotFoundInCcd() {
        when(ccdService.findCaseBy(singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress), idamTokens))
                .thenReturn(singletonList(createCaseDetails(someCaseId, "caseref", "firstname", "lastname")));

        CohOnlineHearings emptyCohOnlineHearings = new CohOnlineHearings(Collections.emptyList());
        when(cohService.getOnlineHearing(someCaseId)).thenReturn(emptyCohOnlineHearings);

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);
        assertThat(onlineHearing.isPresent(), is(false));
    }

    @Test(expected = CaseNotCorException.class)
    public void noOnlineHearingIfFoundInCcdButNotAnOnlineResolutionAppeal() {
        when(ccdService.findCaseBy(singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress), idamTokens))
                .thenReturn(singletonList(createCaseDetails(someCaseId, "caseref", "firstname", "lastname", "paper")));

        underTest.getOnlineHearing(someEmailAddress);
    }

    @Test
    public void noOnlineHearingIfNotFoundInCOh() {
        when(ccdService.findCaseBy(singletonMap("case.subscriptions.appellantSubscription.email", someEmailAddress), idamTokens))
                .thenReturn(emptyList());

        Optional<OnlineHearing> onlineHearing = underTest.getOnlineHearing(someEmailAddress);
        assertThat(onlineHearing.isPresent(), is(false));
    }

    private SscsCaseDetails createCaseDetails(Long caseId, String expectedCaseReference, String firstName, String lastName) {
        return createCaseDetails(caseId, expectedCaseReference, firstName, lastName, "cor");
    }

    private SscsCaseDetails createCaseDetails(Long caseId, String expectedCaseReference, String firstName, String lastName, String hearingType) {
        return SscsCaseDetails.builder()
                .id(caseId)
                .data(SscsCaseData.builder()
                        .caseReference(expectedCaseReference)
                        .onlinePanel(OnlinePanel.builder().assignedTo("someJudge").build())
                        .appeal(Appeal.builder()
                                .hearingType(hearingType)
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
                                .hearingType("paper")
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
