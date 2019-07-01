package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someOnlineHearing;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.util.PostcodeUtil;

public class CitizenLoginServiceTest {

    private static final String SUBSCRIPTION_EMAIL_ADDRESS = "someEmail@exaple.com";
    private static final String APPEAL_POSTCODE = "CM11 1AB";

    private CorCcdService corCcdService;
    private CaseDetails case1;
    private CaseDetails case2;
    private SscsCcdConvertService sscsCcdConvertService;
    private CitizenLoginService underTest;
    private SscsCaseDetails sscsCaseDetailsWithDifferentTya;
    private IdamTokens citizenIdamTokens;
    private IdamTokens serviceIdamTokens;
    private String tya;
    private PostcodeUtil postcodeUtil;
    private OnlineHearingService onlineHearingService;

    @Before
    public void setUp() {
        citizenIdamTokens = IdamTokens.builder()
                .userId("someUserId")
                .build();
        corCcdService = mock(CorCcdService.class);
        case1 = mock(CaseDetails.class);
        case2 = mock(CaseDetails.class);
        List<CaseDetails> cases = Arrays.asList(case1, case2);
        when(corCcdService.searchForCitizen(citizenIdamTokens)).thenReturn(cases);
        sscsCcdConvertService = mock(SscsCcdConvertService.class);

        IdamService idamService = mock(IdamService.class);
        serviceIdamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(serviceIdamTokens);
        postcodeUtil = mock(PostcodeUtil.class);
        when(postcodeUtil.hasAppellantPostcode(any(SscsCaseDetails.class), eq(APPEAL_POSTCODE))).thenReturn(true);
        onlineHearingService = mock(OnlineHearingService.class);

        underTest = new CitizenLoginService(corCcdService, sscsCcdConvertService, idamService, postcodeUtil, onlineHearingService);
        sscsCaseDetailsWithDifferentTya = createSscsCaseDetailsWithAppellantSubscription("anotherTya");
        tya = "123-123-123-123";
    }

    @Test
    public void findsCasesAlreadyAssociatedWithCitizen() {
        SscsCaseDetails sscsCaseDetails1 = SscsCaseDetails.builder().id(111L).build();
        SscsCaseDetails sscsCaseDetails2 = SscsCaseDetails.builder().id(222L).build();
        when(sscsCcdConvertService.getCaseDetails(case1)).thenReturn(sscsCaseDetails1);
        when(sscsCcdConvertService.getCaseDetails(case2)).thenReturn(sscsCaseDetails2);
        OnlineHearing onlineHearing1 = someOnlineHearing(111L);
        when(onlineHearingService.loadOnlineHearingFromCoh(sscsCaseDetails1)).thenReturn(Optional.of(onlineHearing1));
        OnlineHearing onlineHearing2 = someOnlineHearing(222L);
        when(onlineHearingService.loadOnlineHearingFromCoh(sscsCaseDetails2)).thenReturn(Optional.of(onlineHearing2));

        List<OnlineHearing> casesForCitizen = underTest.findCasesForCitizen(citizenIdamTokens, null);

        assertThat(casesForCitizen, is(asList(onlineHearing1, onlineHearing2)));
    }

    @Test
    public void findsCasesAlreadyAssociatedWithCitizenAndAppellantTyaNumber() {
        SscsCaseDetails sscsCaseDetailsWithTya = createSscsCaseDetailsWithAppellantSubscription(tya);

        when(sscsCcdConvertService.getCaseDetails(case1)).thenReturn(sscsCaseDetailsWithDifferentTya);
        when(sscsCcdConvertService.getCaseDetails(case2)).thenReturn(sscsCaseDetailsWithTya);
        OnlineHearing onlineHearing = someOnlineHearing(111L);
        when(onlineHearingService.loadOnlineHearingFromCoh(sscsCaseDetailsWithTya)).thenReturn(Optional.of(onlineHearing));

        List<OnlineHearing> casesForCitizen = underTest.findCasesForCitizen(citizenIdamTokens, tya);

        assertThat(casesForCitizen, is(singletonList(onlineHearing)));
    }

    @Test
    public void findsCasesAlreadyAssociatedWithCitizenAndAppointeeTyaNumber() {
        SscsCaseDetails sscsCaseDetailsWithTya = createSscsCaseDetailsWithAppointeeSubscription(tya);

        when(sscsCcdConvertService.getCaseDetails(case1)).thenReturn(sscsCaseDetailsWithDifferentTya);
        when(sscsCcdConvertService.getCaseDetails(case2)).thenReturn(sscsCaseDetailsWithTya);

        OnlineHearing onlineHearing = someOnlineHearing(111L);
        when(onlineHearingService.loadOnlineHearingFromCoh(sscsCaseDetailsWithTya)).thenReturn(Optional.of(onlineHearing));

        List<OnlineHearing> casesForCitizen = underTest.findCasesForCitizen(citizenIdamTokens, tya);

        assertThat(casesForCitizen, is(singletonList(onlineHearing)));
    }

    @Test
    public void findsCasesAlreadyAssociatedWithCitizenAndRepTyaNumber() {
        SscsCaseDetails sscsCaseDetailsWithTya = createSscsCaseDetailsWithRepSubscription(tya);

        when(sscsCcdConvertService.getCaseDetails(case1)).thenReturn(sscsCaseDetailsWithDifferentTya);
        when(sscsCcdConvertService.getCaseDetails(case2)).thenReturn(sscsCaseDetailsWithTya);
        OnlineHearing onlineHearing = someOnlineHearing(111L);
        when(onlineHearingService.loadOnlineHearingFromCoh(sscsCaseDetailsWithTya)).thenReturn(Optional.of(onlineHearing));

        List<OnlineHearing> casesForCitizen = underTest.findCasesForCitizen(citizenIdamTokens, tya);

        assertThat(casesForCitizen, is(singletonList(onlineHearing)));
    }

    @Test
    public void associatesUserWithCaseAppellant() {
        SscsCaseDetails expectedCase = createSscsCaseDetailsWithAppellantSubscription(tya);
        when(corCcdService.findCaseByAppealNumber(tya, serviceIdamTokens))
                .thenReturn(expectedCase);
        OnlineHearing expectedOnlineHearing = someOnlineHearing(123L);
        when(onlineHearingService.loadOnlineHearingFromCoh(expectedCase)).thenReturn(Optional.of(expectedOnlineHearing));
        Optional<OnlineHearing> sscsCaseDetails = underTest.associateCaseToCitizen(
                citizenIdamTokens, tya, SUBSCRIPTION_EMAIL_ADDRESS, APPEAL_POSTCODE
        );

        long expectedCaseId = expectedCase.getId();
        verify(corCcdService).addUserToCase(citizenIdamTokens.getUserId(), expectedCaseId);
        assertThat(sscsCaseDetails.isPresent(), is(true));
        assertThat(sscsCaseDetails.get(), is(expectedOnlineHearing));
    }

    @Test
    public void associatesUserWithCaseAppointee() {
        SscsCaseDetails expectedCase = createSscsCaseDetailsWithAppointeeSubscription(tya);
        when(corCcdService.findCaseByAppealNumber(tya, serviceIdamTokens))
                .thenReturn(expectedCase);
        OnlineHearing expectedOnlineHearing = someOnlineHearing(123L);
        when(onlineHearingService.loadOnlineHearingFromCoh(expectedCase)).thenReturn(Optional.of(expectedOnlineHearing));
        Optional<OnlineHearing> sscsCaseDetails = underTest.associateCaseToCitizen(
                citizenIdamTokens, tya, SUBSCRIPTION_EMAIL_ADDRESS, APPEAL_POSTCODE
        );

        long expectedCaseId = expectedCase.getId();
        verify(corCcdService).addUserToCase(citizenIdamTokens.getUserId(), expectedCaseId);
        assertThat(sscsCaseDetails.isPresent(), is(true));
        assertThat(sscsCaseDetails.get(), is(expectedOnlineHearing));
    }

    @Test
    public void associatesUserWithCaseRep() {
        SscsCaseDetails expectedCase = createSscsCaseDetailsWithRepSubscription(tya);
        when(corCcdService.findCaseByAppealNumber(tya, serviceIdamTokens))
                .thenReturn(expectedCase);
        OnlineHearing expectedOnlineHearing = someOnlineHearing(123L);
        when(onlineHearingService.loadOnlineHearingFromCoh(expectedCase)).thenReturn(Optional.of(expectedOnlineHearing));
        Optional<OnlineHearing> sscsCaseDetails = underTest.associateCaseToCitizen(
                citizenIdamTokens, tya, SUBSCRIPTION_EMAIL_ADDRESS, APPEAL_POSTCODE
        );

        long expectedCaseId = expectedCase.getId();
        verify(corCcdService).addUserToCase(citizenIdamTokens.getUserId(), expectedCaseId);
        assertThat(sscsCaseDetails.isPresent(), is(true));
        assertThat(sscsCaseDetails.get(), is(expectedOnlineHearing));
    }

    @Test
    public void cannotAssociatesUserWithCaseAsEmailIncorrect() {
        SscsCaseDetails expectedCase = createSscsCaseDetailsWithAppellantSubscription(tya);
        when(corCcdService.findCaseByAppealNumber(tya, serviceIdamTokens))
                .thenReturn(expectedCase);
        Optional<OnlineHearing> sscsCaseDetails = underTest.associateCaseToCitizen(
                citizenIdamTokens, tya, "someOtherEmail@example.com", APPEAL_POSTCODE
        );

        verify(corCcdService, never()).addUserToCase(any(String.class), anyLong());
        assertThat(sscsCaseDetails.isPresent(), is(false));
    }

    @Test
    public void cannotAssociatesUserWithCaseAsPostcodeIncorrect() {
        SscsCaseDetails expectedCase = createSscsCaseDetailsWithAppellantSubscription(tya);
        when(corCcdService.findCaseByAppealNumber(tya, serviceIdamTokens))
                .thenReturn(expectedCase);
        Optional<OnlineHearing> sscsCaseDetails = underTest.associateCaseToCitizen(
                citizenIdamTokens, tya, SUBSCRIPTION_EMAIL_ADDRESS, "someOtherPostcode"
        );

        verify(corCcdService, never()).addUserToCase(any(String.class), anyLong());
        assertThat(sscsCaseDetails.isPresent(), is(false));
    }

    @Test
    public void cannotAssociatesUserWithCaseAsCaseNotFound() {
        String someOtherPostcode = "someOtherPostcode";
        when(postcodeUtil.hasAppellantPostcode(any(SscsCaseDetails.class), eq(someOtherPostcode))).thenReturn(false);

        when(corCcdService.findCaseByAppealNumber(tya, serviceIdamTokens))
                .thenReturn(null);
        Optional<OnlineHearing> sscsCaseDetails = underTest.associateCaseToCitizen(
                citizenIdamTokens, tya, SUBSCRIPTION_EMAIL_ADDRESS, someOtherPostcode
        );

        verify(corCcdService, never()).addUserToCase(any(String.class), anyLong());
        assertThat(sscsCaseDetails.isPresent(), is(false));
    }

    private SscsCaseDetails createSscsCaseDetailsWithAppellantSubscription(String tya) {
        return createSscsCaseDetails(Subscriptions.builder()
                .appellantSubscription(Subscription.builder()
                        .tya(tya)
                        .email(SUBSCRIPTION_EMAIL_ADDRESS)
                        .build())
                .build());
    }

    private SscsCaseDetails createSscsCaseDetailsWithAppointeeSubscription(String tya) {
        return createSscsCaseDetails(Subscriptions.builder()
                .appointeeSubscription(Subscription.builder()
                        .tya(tya)
                        .email(SUBSCRIPTION_EMAIL_ADDRESS)
                        .build())
                .build());
    }

    private SscsCaseDetails createSscsCaseDetailsWithRepSubscription(String tya) {
        return createSscsCaseDetails(Subscriptions.builder()
                .representativeSubscription(Subscription.builder()
                        .tya(tya)
                        .email(SUBSCRIPTION_EMAIL_ADDRESS)
                        .build())
                .build());
    }

    private SscsCaseDetails createSscsCaseDetails(Subscriptions subscriptions) {
        return SscsCaseDetails.builder()
                .id(123456789L)
                .data(SscsCaseData.builder()
                        .appeal(Appeal.builder()
                                .appellant(Appellant.builder()
                                        .address(Address.builder()
                                                .postcode(APPEAL_POSTCODE)
                                                .build())
                                        .build())
                                .build())

                        .subscriptions(subscriptions)
                        .build())
                .build();
    }
}