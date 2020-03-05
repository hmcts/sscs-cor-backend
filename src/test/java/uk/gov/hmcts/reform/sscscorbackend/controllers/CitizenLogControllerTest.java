package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.UserDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.UserLogType;
import uk.gov.hmcts.reform.sscscorbackend.service.CitizenLoginService;

public class CitizenLogControllerTest {

    private CitizenLogController underTest;
    private CitizenLoginService citizenLoginService;
    private IdamService idamService;
    private UserDetails idamUserDetails;

    @Before
    public void setUp() {
        citizenLoginService = mock(CitizenLoginService.class);
        idamService = mock(IdamService.class);
        idamUserDetails = UserDetails.builder().id("userId").build();
        underTest = new CitizenLogController(citizenLoginService, idamService);
    }

    @Test
    public void logUserWithCase() {
        String oauthToken = "oAuth";
        String caseId = "123456";
        String userId = "userId";

        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuth");
        when(idamService.getUserDetails(oauthToken)).thenReturn(idamUserDetails);
        ResponseEntity<Void> response = underTest.logUserWithCase(oauthToken, caseId, UserLogType.USER_LOGIN_TIMESTAMP);

        verify(citizenLoginService).findAndUpdateCaseLastLoggedIntoMya(argThat(tokens -> userId.equals(tokens.getUserId()) && oauthToken.equals(tokens.getIdamOauth2Token())),
                eq(caseId));
        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }
}
