package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someOnlineHearing;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscscorbackend.domain.AssociateCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.service.CitizenLoginService;

public class CitizenControllerTest {

    private CitizenController underTest;
    private CitizenLoginService citizenLoginService;
    private IdamService idamService;

    @Before
    public void setUp() {
        citizenLoginService = mock(CitizenLoginService.class);
        idamService = mock(IdamService.class);
        underTest = new CitizenController(citizenLoginService, idamService);
    }

    @Test
    public void getOnlineHearings() {
        String oauthToken = "oAuth";
        String tya = "tya";
        String userId = "userId";

        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuth");
        when(idamService.getUserId(oauthToken)).thenReturn(userId);
        List<OnlineHearing> expectedOnlineHearings = asList(someOnlineHearing(1), someOnlineHearing(2));
        when(citizenLoginService.findCasesForCitizen(
                argThat(tokens -> userId.equals(tokens.getUserId()) && oauthToken.equals(tokens.getIdamOauth2Token())),
                eq(tya))
        ).thenReturn(expectedOnlineHearings);

        ResponseEntity<List<OnlineHearing>> onlineHearings = underTest.getOnlineHearingsForTyaNumber(oauthToken, tya);

        assertThat(onlineHearings.getStatusCode(), is(HttpStatus.OK));
        assertThat(onlineHearings.getBody(), is(expectedOnlineHearings));
    }

    @Test
    public void associateUserWithCase() {
        String oauthToken = "oAuth";
        String tya = "tya";
        String userId = "userId";
        String email = "someemail@example.com";
        String postcode = "somePostcode";

        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuth");
        when(idamService.getUserId(oauthToken)).thenReturn(userId);
        OnlineHearing onlineHearing = someOnlineHearing();
        when(citizenLoginService.associateCaseToCitizen(
                argThat(tokens -> userId.equals(tokens.getUserId()) && oauthToken.equals(tokens.getIdamOauth2Token())),
                eq(tya),
                eq(email),
                eq(postcode)
        )).thenReturn(Optional.of(onlineHearing));

        ResponseEntity<OnlineHearing> response = underTest.associateUserWithCase(oauthToken, tya, new AssociateCaseDetails(email, postcode));

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(onlineHearing));
    }

    @Test
    public void cannotAssociateUserWithCase() {
        String oauthToken = "oAuth";
        String tya = "tya";
        String userId = "userId";
        String email = "someemail@example.com";
        String postcode = "somePostcode";

        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuth");
        when(idamService.getUserId(oauthToken)).thenReturn(userId);
        when(citizenLoginService.associateCaseToCitizen(null, tya, email, postcode))
                .thenReturn(Optional.empty());

        ResponseEntity<OnlineHearing> response = underTest.associateUserWithCase(oauthToken, tya, new AssociateCaseDetails(email, postcode));

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN));
    }
}
