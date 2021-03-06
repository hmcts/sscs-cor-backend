package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static java.util.stream.Collectors.joining;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscscorbackend.exception.SscsCorBackendException;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;

@RestController
@ConditionalOnProperty("create_ccd_endpoint")
public class CreateCaseController {

    private final CorCcdService ccdService;
    private final IdamService idamService;

    public CreateCaseController(
            @Autowired CorCcdService ccdService,
            @Autowired IdamService idamService
    ) {
        this.ccdService = ccdService;
        this.idamService = idamService;
    }

    @ApiOperation(value = "Create a case",
            notes = "Creates a case in CCD with an online panel which can the be used for a online hearing. This " +
                    "endpoint is just for test and should only be present in test environments. The email address " +
                    "used will need to be unique for all other cases in CCD with an online panel if we want to load " +
                    "it for the COR process."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Case has been created")
    })
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/case", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> createCase(
            @ApiParam(value = "email address of the appellant must be unique in CCD", example = "foo@bar.com", required = true)
            @RequestParam("email")String email,
            @ApiParam(value = "mobile number of appellant. Optional if not set will not subscribe for sms.")
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "hearingType", defaultValue = "cor") String hearingType
    ) throws URISyntaxException {
        SscsCaseDetails caseDetails = ccdService.createCase(
                createSscsCase(email, mobile, hearingType),
                EventType.SYA_APPEAL_CREATED.getCcdType(),
                "SSCS - appeal created event", "Created SSCS",
                idamService.getIdamTokens()
        );

        HashMap<String, String> body = new HashMap<>();
        body.put("id", caseDetails.getId().toString());
        body.put("case_reference", caseDetails.getData().getCaseReference());
        body.put("appellant_tya", caseDetails.getData().getSubscriptions().getAppellantSubscription().getTya());
        return ResponseEntity.created(new URI("case/someId")).body(body);
    }

    private SscsCaseData createSscsCase(String email, String mobile, String hearingType) {
        InputStream caseStream = getClass().getClassLoader().getResourceAsStream("json/ccd_case.json");
        String caseAsString = new BufferedReader(new InputStreamReader(caseStream)).lines().collect(joining("\n"));
        SscsCaseData sscsCaseData;
        try {
            sscsCaseData = new ObjectMapper().readValue(caseAsString, SscsCaseData.class);

            Event events = Event.builder()
                    .value(EventDetails.builder()
                            .type("appealCreated")
                            .description("Some Events")
                            .date("2017-05-24T14:01:18.243")
                            .build())
                    .build();

            sscsCaseData = sscsCaseData.toBuilder()
                    .events(Collections.singletonList(events))
                    .caseReference("SC285/17/" + new Random().nextInt(90000) + 10000)
                    .subscriptions(
                            Subscriptions.builder()
                                    .appellantSubscription(
                                            Subscription.builder()
                                                    .email(email)
                                                    .mobile(mobile)
                                                    .subscribeEmail("yes")
                                                    .subscribeSms((mobile != null) ? "yes" : "no")
                                                    .tya(UUID.randomUUID().toString())
                                                    .build()
                                    ).build()
                    )
                    .build();
            sscsCaseData.getAppeal().setHearingType(hearingType);
        } catch (IOException e) {
            throw new SscsCorBackendException(e);
        }

        return sscsCaseData;
    }
}
