package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.Statement;
import uk.gov.hmcts.reform.sscscorbackend.service.AppellantStatementService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

public class StatementControllerTest {

    private String onlineHearingId;
    private long caseId;
    private Statement statement;
    private SscsCaseDetails sscsCaseDetails;
    private StatementController statementController;
    private AppellantStatementService appellantStatementService;

    @Before
    public void setUp() {
        onlineHearingId = "someOnlineHearingId";
        caseId = 12345L;
        statement = new Statement("someStatement", "someTya");
        sscsCaseDetails = SscsCaseDetails.builder().id(caseId).build();

        appellantStatementService = mock(AppellantStatementService.class);
        statementController = new StatementController(appellantStatementService);
    }

    @Test
    public void canUploadAStatement() {
        when(appellantStatementService.handleAppellantStatement(onlineHearingId, statement))
                .thenReturn(Optional.of(mock(CohEventActionContext.class)));

        ResponseEntity responseEntity = statementController.uploadStatement(onlineHearingId, statement);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }

    @Test
    public void cannotUploadAStatementIfOnlineHearingNotFound() {
        when(appellantStatementService.handleAppellantStatement(onlineHearingId, statement))
                .thenReturn(Optional.empty());

        ResponseEntity responseEntity = statementController.uploadStatement(onlineHearingId, statement);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }
}