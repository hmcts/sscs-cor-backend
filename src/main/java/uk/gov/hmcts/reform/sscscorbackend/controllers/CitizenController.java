package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.idam.UserDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.AssociateCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.service.CitizenLoginService;

@Slf4j
@RestController
@RequestMapping("/citizen")
public class CitizenController {

    private final CitizenLoginService citizenLoginService;
    private final IdamService idamService;

    @Autowired
    public CitizenController(CitizenLoginService citizenLoginService,
                             IdamService idamService) {
        this.citizenLoginService = citizenLoginService;
        this.idamService = idamService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Loads cases associated with a citizen",
            notes = "Loads the cases that have been associated with a citizen in CCD. Gets the user from the token " +
                    "in the Authorization header."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A list of the hearings associated with a citizen.")
    })
    @PreAuthorize("hasAuthority('citizen')")
    public ResponseEntity<List<OnlineHearing>> getOnlineHearings(
            @ApiParam(value = "user authorisation header", example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW") @RequestHeader(AUTHORIZATION) String authorisation
    ) {
        return getOnlineHearingsForTyaNumber(authorisation, "");
    }

    @RequestMapping(method = RequestMethod.GET, value = "{tya}")
    @ApiOperation(value = "Loads cases associated with a citizen",
            notes = "Loads the cases that have been associated with a citizen in CCD. If a tya parameter is provided " +
                    "then the list will be limited to the case with the tya number or be empty if the case has not " +
                    "been associated with the user. Gets the user from the token in the Authorization header."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A list of the hearings associated with a citizen and tya number.")
    })
    @PreAuthorize("hasAuthority('citizen')")
    public ResponseEntity<List<OnlineHearing>> getOnlineHearingsForTyaNumber(
            @ApiParam(value = "user authorisation header", example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW") @RequestHeader(AUTHORIZATION) String authorisation,
            @ApiParam(value = "tya number for an user and appeal", example = "A123-B123-c123-Dgdg") @PathVariable("tya") String tya) {

        List<OnlineHearing> casesForCitizen = citizenLoginService.findCasesForCitizen(getUserTokens(authorisation), tya);

        return ResponseEntity.ok(casesForCitizen);
    }

    private IdamTokens getUserTokens(String oauth2Token) {
        UserDetails userDetails = idamService.getUserDetails(oauth2Token);
        return IdamTokens.builder()
                .idamOauth2Token(oauth2Token)
                .serviceAuthorization(idamService.generateServiceAuthorization())
                .userId(userDetails.getId())
                .email(userDetails.getEmail())
                .roles(userDetails.getRoles())
                .build();
    }

    @RequestMapping(method = RequestMethod.POST, value = "{tya}")
    @ApiOperation(value = "Associates a case with a citizen",
            notes = "Associates a case in CCD with a citizen idam user. Checks the TYA number, email and postcode are " +
                    "all match the case before associating the case with the idam user."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The citizen has been associated with the case"),
            @ApiResponse(code = 403, message = "The citizen cannot be associated with the case, either the user does not " +
                    "exists or the email/postcode do not match the case the tya number is for."),
    })
    @PreAuthorize("hasAuthority('citizen')")
    public ResponseEntity<OnlineHearing> associateUserWithCase(
            @ApiParam(value = "user authorisation header", example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW") @RequestHeader(AUTHORIZATION) String authorisation,
            @ApiParam(value = "tya number for an user and appeal", example = "A123-B123-c123-Dgdg") @PathVariable("tya") String tya,
            @ApiParam(value = "email address of the appellant", example = "foo@bar.com") @RequestBody() AssociateCaseDetails associateCaseDetails
    ) {
        Optional<OnlineHearing> onlineHearing = citizenLoginService.associateCaseToCitizen(
                getUserTokens(authorisation),
                tya,
                associateCaseDetails.getEmail(),
                associateCaseDetails.getPostcode()
        );

        return onlineHearing
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(FORBIDDEN.value()).build());
    }

    @ApiOperation(value = "Log time against a case for a citizen",
            notes = "Log time against a case a case for a citizen idam user. Checks the email is " +
                    "match the case before logging the mya time with the idam user."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No content, the time has been logged against the case"),
            @ApiResponse(code = 403, message = "The time cannot be logged against the case, either the user does not " +
                    "exists or the email do not match the case.")
    })
    @PutMapping(value = "/cases/{caseId}/log", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> logUserWithCase(
            @ApiParam(value = "user authorisation header", example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW") @RequestHeader(AUTHORIZATION) String authorisation,
            @ApiParam(value = "case id for an user and appeal", example = "12345678") @PathVariable("caseId") String caseId
    ) {

        IdamTokens citizenTokens = getUserTokens(authorisation);
        citizenLoginService.findAndUpdateCaseLastLoggedIntoMya(citizenTokens, caseId);

        return ResponseEntity.noContent().build();
    }
}
