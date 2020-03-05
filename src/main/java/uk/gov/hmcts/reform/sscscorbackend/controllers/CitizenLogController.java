package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.idam.UserDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.UserLogType;
import uk.gov.hmcts.reform.sscscorbackend.service.CitizenLoginService;

@Slf4j
@RestController
public class CitizenLogController {

    private final CitizenLoginService citizenLoginService;
    private final IdamService idamService;

    @Autowired
    public CitizenLogController(CitizenLoginService citizenLoginService,
                                IdamService idamService) {
        this.citizenLoginService = citizenLoginService;
        this.idamService = idamService;
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

    @ApiOperation(value = "Log time against a case for a citizen",
            notes = "Log time against a case a case for a citizen idam user. Checks the email is " +
                    "match the case before logging the mya time with the idam user."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No content, the time has been logged against the case"),
            @ApiResponse(code = 403, message = "The time cannot be logged against the case, either the user does not " +
                    "exists or the email do not match the case."),
    })
    @PutMapping(value = "/cases/{caseId}/log", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> logUserWithCase(
            @ApiParam(value = "user authorisation header", example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW") @RequestHeader(AUTHORIZATION) String authorisation,
            @ApiParam(value = "case id for an user and appeal", example = "12345678") @PathVariable("caseId") String caseId,
            @ApiParam(value = "log timestamp", example = "") @RequestBody() UserLogType userLogType
    ) {

        citizenLoginService.findAndUpdateCaseLastLoggedIntoMya(getUserTokens(authorisation), caseId);

        return ResponseEntity.noContent().build();
    }
}
