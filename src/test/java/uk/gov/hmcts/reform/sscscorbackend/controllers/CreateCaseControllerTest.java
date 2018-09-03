package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscs.ccd.CcdClient;
import uk.gov.hmcts.reform.sscs.ccd.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

public class CreateCaseControllerTest {

    private CcdClient ccdClient;
    private CcdRequestDetails ccdRequestDetails;

    @Before
    public void setUp() throws Exception {
        ccdClient = mock(CcdClient.class);
        ccdRequestDetails = CcdRequestDetails.builder().build();
    }

    @Test
    public void createCase() throws URISyntaxException {
        Long caseId = 123L;
        String caseRef = "someCaseRef";
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().id(caseId).data(SscsCaseData.builder().caseReference(caseRef).build()).build();
        when(ccdClient.createCase(any(CcdRequestDetails.class), any(SscsCaseData.class))).thenReturn(sscsCaseDetails);
        CreateCaseController createCaseController = new CreateCaseController(ccdClient, ccdRequestDetails);

        ResponseEntity<Map<String, String>> createCaseResponse = createCaseController.createCase("someEmail", "someMobile");

        assertThat(createCaseResponse.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(createCaseResponse.getBody().get("id"), is(caseId.toString()));
        assertThat(createCaseResponse.getBody().get("case_reference"), is(caseRef));
    }
}