package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
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
    public ResponseEntity<List<OnlineHearing>> getOnlineHearings(
            @ApiParam(value = "user authorisation header", example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW") @RequestHeader(AUTHORIZATION) String authorisation
    ) {
        return getOnlineHearingsForTyaNumber(authorisation, "");
    }

    @RequestMapping(method = RequestMethod.GET, value = "{tya}")
    public ResponseEntity<List<OnlineHearing>> getOnlineHearingsForTyaNumber(
            @ApiParam(value = "user authorisation header", example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW") @RequestHeader(AUTHORIZATION) String authorisation,
            @ApiParam(value = "tya number for an user and appeal", example = "A123-B123-c123-Dgdg") @PathVariable("tya") String tya) {
        List<OnlineHearing> casesForCitizen = citizenLoginService.findCasesForCitizen(getUserTokens(authorisation), tya);

        return ResponseEntity.ok(casesForCitizen);
    }

    private IdamTokens getUserTokens(String oauth2Token) {
        return IdamTokens.builder()
                .idamOauth2Token(oauth2Token)
                .serviceAuthorization(idamService.generateServiceAuthorization())
                .userId(idamService.getUserId(oauth2Token))
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
}
