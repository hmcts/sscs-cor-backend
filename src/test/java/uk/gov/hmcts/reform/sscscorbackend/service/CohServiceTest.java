package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import feign.FeignException;
import org.junit.Test;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

public class CohServiceTest {
    @Test
    public void canExtendADeadline() {
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
        CohClient cohClient = mock(CohClient.class);
        CohService cohService = new CohService(authTokenGenerator, cohClient);

        boolean haveExtendedDeadline = cohService.extendQuestionRoundDeadline("someOnlineHearingId");

        assertThat(haveExtendedDeadline, is(true));
    }

    @Test
    public void cannotExtendADeadline() {
        String someOnlineHearingId = "someOnlineHearingId";
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
        CohClient cohClient = mock(CohClient.class);
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(424);
        doThrow(feignException)
                .when(cohClient).extendQuestionRoundDeadline(any(), any(), eq(someOnlineHearingId), eq("{}"));

        CohService cohService = new CohService(authTokenGenerator, cohClient);

        boolean haveExtendedDeadline = cohService.extendQuestionRoundDeadline(someOnlineHearingId);

        assertThat(haveExtendedDeadline, is(false));
    }

    @Test(expected = FeignException.class)
    public void extendADeadlineThrowsException() {
        String someOnlineHearingId = "someOnlineHearingId";
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
        CohClient cohClient = mock(CohClient.class);
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(500);
        doThrow(feignException)
                .when(cohClient).extendQuestionRoundDeadline(any(), any(), eq(someOnlineHearingId), eq("{}"));

        CohService cohService = new CohService(authTokenGenerator, cohClient);

        cohService.extendQuestionRoundDeadline(someOnlineHearingId);
    }

}