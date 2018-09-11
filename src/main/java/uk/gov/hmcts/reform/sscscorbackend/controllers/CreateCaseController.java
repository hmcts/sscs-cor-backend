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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.ccd.CcdClient;
import uk.gov.hmcts.reform.sscs.ccd.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

@RestController
@ConditionalOnProperty("create_ccd_endpoint")
public class CreateCaseController {

    private final CcdClient ccdClient;
    private final CcdRequestDetails ccdRequestDetails;

    public CreateCaseController(@Autowired CcdClient ccdClient, @Autowired CcdRequestDetails ccdRequestDetails) {
        this.ccdClient = ccdClient;
        this.ccdRequestDetails = ccdRequestDetails;
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
    @RequestMapping(value = "/case", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> createCase(
            @ApiParam(value = "email address of the appellant must be unique in CCD", example = "foo@bar.com", required = true)
            @RequestParam("email")String email,
            @ApiParam(value = "mobile number of appellant. Optional if not set will not subscribe for sms.")
            @RequestParam(value = "mobile", required = false) String mobile
    ) throws URISyntaxException {
        SscsCaseDetails caseDetails = ccdClient.createCase(ccdRequestDetails, createSscsCase(email, mobile));

        HashMap<String, String> body = new HashMap<>();
        body.put("id", caseDetails.getId().toString());
        body.put("case_reference", caseDetails.getData().getCaseReference());
        return ResponseEntity.created(new URI("case/someId")).body(body);
    }

    private SscsCaseData createSscsCase(String email, String mobile) {
        InputStream caseStream = getClass().getClassLoader().getResourceAsStream("json/ccd_case.json");
        String caseAsString = new BufferedReader(new InputStreamReader(caseStream)).lines().collect(joining("\n"));
        SscsCaseData sscsCaseData;
        try {
            sscsCaseData = new ObjectMapper().readValue(caseAsString, SscsCaseData.class);

            sscsCaseData = sscsCaseData.toBuilder()
                    .onlinePanel(OnlinePanel.builder()
                            .assignedTo("someJudge")
                            .disabilityQualifiedMember("disabilityQualifiedMember")
                            .medicalMember("medicalMember")
                            .build())
                    .caseReference("SC285/17/" + new Random().nextInt(90000) + 10000)
                    .subscriptions(
                            Subscriptions.builder()
                                    .appellantSubscription(
                                            Subscription.builder()
                                                    .email(email)
                                                    .mobile(mobile)
                                                    .subscribeEmail("yes")
                                                    .subscribeSms((mobile != null) ? "yes" : "no")
                                                    .build()
                                    ).build()
                    ).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sscsCaseData;
    }
}
