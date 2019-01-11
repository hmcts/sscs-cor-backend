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
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohClient;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;

public class CohServiceTest {

    private IdamService idamService;

    @Before
    public void setup() {
        idamService = mock(IdamService.class);
        when(idamService.getIdamTokens()).thenReturn(
                IdamTokens.builder().idamOauth2Token("oath token").serviceAuthorization("service auth").build()
        );
    }

    @Test
    public void canExtendADeadline() {
        CohClient cohClient = mock(CohClient.class);
        CohService cohService = new CohService(idamService, cohClient);

        boolean haveExtendedDeadline = cohService.extendQuestionRoundDeadline("someOnlineHearingId");

        assertThat(haveExtendedDeadline, is(true));
    }

    @Test
    public void cannotExtendADeadline() {
        String someOnlineHearingId = "someOnlineHearingId";
        CohClient cohClient = mock(CohClient.class);
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(424);
        doThrow(feignException)
                .when(cohClient).extendQuestionRoundDeadline(any(), any(), eq(someOnlineHearingId), eq("{}"));

        CohService cohService = new CohService(idamService, cohClient);

        boolean haveExtendedDeadline = cohService.extendQuestionRoundDeadline(someOnlineHearingId);

        assertThat(haveExtendedDeadline, is(false));
    }

    @Test(expected = FeignException.class)
    public void extendADeadlineThrowsException() {
        String someOnlineHearingId = "someOnlineHearingId";
        CohClient cohClient = mock(CohClient.class);
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(500);
        doThrow(feignException)
                .when(cohClient).extendQuestionRoundDeadline(any(), any(), eq(someOnlineHearingId), eq("{}"));

        CohService cohService = new CohService(idamService, cohClient);

        cohService.extendQuestionRoundDeadline(someOnlineHearingId);
    }

}