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
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

public class CreateCaseControllerTest {

    private CcdService ccdService;
    private CcdRequestDetails ccdRequestDetails;
    private IdamService idamService;

    @Before
    public void setUp() {
        ccdService = mock(CcdService.class);
        ccdRequestDetails = CcdRequestDetails.builder().build();

        idamService = mock(IdamService.class);
        when(idamService.getIdamTokens()).thenReturn(IdamTokens.builder().build());
    }

    @Test
    public void createCase() throws URISyntaxException {
        Long caseId = 123L;
        String caseRef = "someCaseRef";
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().id(caseId).data(SscsCaseData.builder().caseReference(caseRef).build()).build();
        when(ccdService.createCase(any(SscsCaseData.class), any(IdamTokens.class))).thenReturn(sscsCaseDetails);
        CreateCaseController createCaseController = new CreateCaseController(ccdService, idamService);

        ResponseEntity<Map<String, String>> createCaseResponse = createCaseController.createCase("someEmail", "someMobile");

        assertThat(createCaseResponse.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(createCaseResponse.getBody().get("id"), is(caseId.toString()));
        assertThat(createCaseResponse.getBody().get("case_reference"), is(caseRef));
    }
}