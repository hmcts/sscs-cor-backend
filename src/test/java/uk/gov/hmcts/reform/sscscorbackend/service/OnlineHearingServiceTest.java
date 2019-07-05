package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.*;

import java.util.ArrayList;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.domain.Decision;
import uk.gov.hmcts.reform.sscscorbackend.domain.FinalDecision;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.domain.TribunalViewResponse;
import uk.gov.hmcts.reform.sscscorbackend.service.email.DecisionEmailService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.api.CcdHistoryEvent;
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
    private DecisionEmailService decisionEmailService;

    @Before
    public void setUp() {
        cohService = mock(CohService.class);
        ccdService = mock(CorCcdService.class);
        decisionExtractor = mock(DecisionExtractor.class);
        idamTokens = IdamTokens.builder().build();
        idamService = mock(IdamService.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);

        amendPanelMembersService = mock(AmendPanelMembersService.class);
        decisionEmailService = mock(DecisionEmailService.class);
        underTest = new OnlineHearingService(cohService, ccdService, idamService, decisionExtractor, amendPanelMembersService, false, decisionEmailService);

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
        underTest = new OnlineHearingService(cohService, ccdService, idamService, decisionExtractor, amendPanelMembersService, true, decisionEmailService);
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

    @Test
    public void getsACcdCaseFromAHearing() {
        String onlineHearingId = "someOnlineHearingId";
        CohOnlineHearing cohOnlineHearing = someCohOnlineHearing();
        when(cohService.getOnlineHearing(onlineHearingId)).thenReturn(cohOnlineHearing);
        SscsCaseDetails caseDetails = createCaseDetails(someCaseId, "someCaseReference", "firstName", "lastName");
        when(ccdService.getByCaseId(cohOnlineHearing.getCcdCaseId(), idamTokens)).thenReturn(caseDetails);

        Optional<SscsCaseDetails> sscsCaseDetails = underTest.getCcdCase(onlineHearingId);

        assertThat(sscsCaseDetails.isPresent(), is(true));
        assertThat(sscsCaseDetails.get(), is(caseDetails));
    }

    @Test
    public void getsACcdCaseButCannotFindHearing() {
        String onlineHearingId = "someOnlineHearingId";
        when(cohService.getOnlineHearing(onlineHearingId)).thenReturn(null);

        Optional<SscsCaseDetails> sscsCaseDetails = underTest.getCcdCase(onlineHearingId);

        assertThat(sscsCaseDetails.isPresent(), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void cannotACcdCaseFromAHearing() {
        String onlineHearingId = "someOnlineHearingId";
        CohOnlineHearing cohOnlineHearing = someCohOnlineHearing();
        when(cohService.getOnlineHearing(onlineHearingId)).thenReturn(cohOnlineHearing);
        when(ccdService.getByCaseId(cohOnlineHearing.getCcdCaseId(), idamTokens)).thenReturn(null);

        underTest.getCcdCase(onlineHearingId);
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

        CohOnlineHearings emptyCohOnlineHearings = new CohOnlineHearings(emptyList());
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

    @Test
    public void loadOnlineHearingIncludesFinalDecision() {
        SscsCaseDetails sscsCaseDetails = createCaseDetails(someCaseId, "caseref", "firstname", "lastname", "online");
        sscsCaseDetails.getData().setIsCorDecision("YES");

        when(cohService.getOnlineHearing(someCaseId)).thenReturn(DataFixtures.someCohOnlineHearings());
        when(ccdService.getHistoryEvents(someCaseId)).thenReturn(singletonList(new CcdHistoryEvent(EventType.FINAL_DECISION.getCcdType())));
        Optional<OnlineHearing> onlineHearing = underTest.loadOnlineHearingFromCoh(sscsCaseDetails);

        assertThat(onlineHearing.isPresent(), is(true));
        assertThat(onlineHearing.get().getFinalDecision().getReason(), is(sscsCaseDetails.getData().getDecisionNotes()));
        assertThat(onlineHearing.get().isHasFinalDecision(), is(true));
    }

    @Test
    public void loadHearingWithCorCase() {
        SscsCaseDetails sscsCaseDetails = createCaseDetails(someCaseId, "caseref", "firstname", "lastname", "online");
        sscsCaseDetails.getData().setIsCorDecision("YES");

        when(cohService.getOnlineHearing(someCaseId)).thenReturn(DataFixtures.someCohOnlineHearings());
        when(ccdService.getHistoryEvents(someCaseId)).thenReturn(singletonList(new CcdHistoryEvent(EventType.FINAL_DECISION.getCcdType())));
        CohDecision cohDecision = someCohDecision();
        when(cohService.getDecision("someOnlineHearingId")).thenReturn(Optional.of(cohDecision));
        CohDecisionReplies cohDecisionReplies = someCohDecisionReplies();
        when(cohService.getDecisionReplies("someOnlineHearingId")).thenReturn(Optional.of(cohDecisionReplies));
        Decision decision = someDecision();
        when(decisionExtractor.extract(someCaseId, cohDecision, cohDecisionReplies.getDecisionReplies().get(0))).thenReturn(decision);

        Optional<OnlineHearing> onlineHearing = underTest.loadHearing(sscsCaseDetails);

        assertThat(onlineHearing.isPresent(), is(true));
        assertThat(onlineHearing.get(), is(new OnlineHearing(
                "someOnlineHearingId",
                "firstname lastname",
                "caseref",
                1234321L,
                decision,
                new FinalDecision("decision notes"),
                true
        )));
    }

    @Test
    public void loadHearingWithoutCorCase() {
        SscsCaseDetails sscsCaseDetails = createCaseDetails(someCaseId, "caseref", "firstname", "lastname", "online");

        when(cohService.getOnlineHearing(someCaseId)).thenReturn(new CohOnlineHearings(emptyList()));

        Optional<OnlineHearing> onlineHearing = underTest.loadHearing(sscsCaseDetails);

        assertThat(onlineHearing.isPresent(), is(true));
        assertThat(onlineHearing.get(), is(new OnlineHearing(
                null,
                "firstname lastname",
                "caseref",
                1234321L,
                null,
                null,
                false
        )));
    }


    @Test
    public void addDecisionReplyAndSendEmailIfDecisionAccepted() {
        String someOnlineHearingId = "someOnlineHearingId";
        String reply = "decision_accepted";
        String reason = "reason";
        CohOnlineHearing cohOnlineHearing = someCohOnlineHearing();
        when(cohService.getOnlineHearing(someOnlineHearingId)).thenReturn(cohOnlineHearing);
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().build();
        when(ccdService.getByCaseId(cohOnlineHearing.getCcdCaseId(), idamTokens)).thenReturn(sscsCaseDetails);

        TribunalViewResponse tribunalViewResponse = new TribunalViewResponse(reply, reason);
        underTest.addDecisionReply(someOnlineHearingId, tribunalViewResponse);

        verify(cohService).addDecisionReply(someOnlineHearingId, new CohDecisionReply(reply, reason));
        verify(decisionEmailService).sendEmail(sscsCaseDetails, tribunalViewResponse);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDecisionReplyExceptionWhenHearingIdDoesNotExist() {
        String someOnlineHearingId = "someOnlineHearingId";
        when(cohService.getOnlineHearing(someOnlineHearingId)).thenReturn(null);

        underTest.addDecisionReply(someOnlineHearingId, new TribunalViewResponse("decision_accepted", "reason"));
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
                        )
                        .decisionNotes("decision notes")
                        .events(new ArrayList<>())
                        .build()
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
