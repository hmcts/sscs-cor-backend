package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohClient;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CreateOnlineHearingRequest;

public class CohServiceTest {

    private IdamService idamService;
    private CohClient cohClient;
    private CohService cohService;
    private String authorisation;
    private String serviceAuthorisation;
    private String someHearingId;

    @Before
    public void setup() {
        idamService = mock(IdamService.class);
        authorisation = "oath token";
        serviceAuthorisation = "service auth";
        when(idamService.getIdamTokens()).thenReturn(
                IdamTokens.builder().idamOauth2Token(authorisation).serviceAuthorization(serviceAuthorisation).build()
        );
        cohClient = mock(CohClient.class);
        cohService = new CohService(idamService, cohClient);
        someHearingId = "someHearingId";
    }

    @Test
    public void canCreateOnlineHearing() {
        CreateOnlineHearingRequest createOnlineHearingRequest = DataFixtures.someRequest();
        when(cohClient.createOnlineHearing(authorisation, serviceAuthorisation, createOnlineHearingRequest)).thenReturn(someHearingId);

        boolean createdOnlineHearing = cohService.createOnlineHearing(createOnlineHearingRequest);
        assertThat(createdOnlineHearing, is(true));
    }

    @Test
    public void onlineHearingHasAlreadyBeenCreated() {
        CreateOnlineHearingRequest createOnlineHearingRequest = DataFixtures.someRequest();
        FeignException feignException = feignExceptionForStatus(409);
        when(cohClient.createOnlineHearing(authorisation, serviceAuthorisation, createOnlineHearingRequest)).thenThrow(feignException);

        boolean createdOnlineHearing = cohService.createOnlineHearing(createOnlineHearingRequest);
        assertThat(createdOnlineHearing, is(false));
    }

    @Test(expected = FeignException.class)
    public void onlineHearingThrowAnotherException() {
        CreateOnlineHearingRequest createOnlineHearingRequest = DataFixtures.someRequest();
        FeignException feignException = feignExceptionForStatus(422);
        when(cohClient.createOnlineHearing(authorisation, serviceAuthorisation, createOnlineHearingRequest)).thenThrow(feignException);

        boolean createdOnlineHearing = cohService.createOnlineHearing(createOnlineHearingRequest);
    }

    @Test
    public void canExtendADeadline() {
        boolean haveExtendedDeadline = cohService.extendQuestionRoundDeadline("someOnlineHearingId");

        assertThat(haveExtendedDeadline, is(true));
    }

    @Test
    public void cannotExtendADeadline() {
        String someOnlineHearingId = "someOnlineHearingId";
        FeignException feignException = feignExceptionForStatus(424);
        doThrow(feignException)
                .when(cohClient).extendQuestionRoundDeadline(any(), any(), eq(someOnlineHearingId), eq("{}"));

        boolean haveExtendedDeadline = cohService.extendQuestionRoundDeadline(someOnlineHearingId);

        assertThat(haveExtendedDeadline, is(false));
    }

    @Test(expected = FeignException.class)
    public void extendADeadlineThrowsException() {
        String someOnlineHearingId = "someOnlineHearingId";
        FeignException feignException = feignExceptionForStatus(500);
        doThrow(feignException)
                .when(cohClient).extendQuestionRoundDeadline(any(), any(), eq(someOnlineHearingId), eq("{}"));

        cohService.extendQuestionRoundDeadline(someOnlineHearingId);
    }

    private FeignException feignExceptionForStatus(int i) {
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(i);
        return feignException;
    }

}