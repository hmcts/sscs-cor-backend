package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.Statement;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreAppellantStatementService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.AppellantStatementPdfData;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

public class StatementControllerTest {

    private String onlineHearingId;
    private OnlineHearingService onlineHearingService;
    private long caseId;
    private Statement statement;
    private SscsCaseDetails sscsCaseDetails;
    private StoreAppellantStatementService storeAppellantStatementService;
    private StatementController statementController;

    @Before
    public void setUp() {
        onlineHearingId = "someOnlineHearingId";
        onlineHearingService = mock(OnlineHearingService.class);
        caseId = 12345L;
        statement = new Statement("someStatement");
        sscsCaseDetails = SscsCaseDetails.builder().id(caseId).build();
        storeAppellantStatementService = mock(StoreAppellantStatementService.class);
        statementController = new StatementController(storeAppellantStatementService, onlineHearingService);
    }

    @Test
    public void canUploadAStatement() {
        when(onlineHearingService.getCcdCase(onlineHearingId)).thenReturn(Optional.of(sscsCaseDetails));
        when(storeAppellantStatementService.storePdf(eq(caseId), eq(onlineHearingId), eq(new AppellantStatementPdfData(sscsCaseDetails, statement))))
                .thenReturn(mock(CohEventActionContext.class));

        ResponseEntity responseEntity = statementController.uploadStatement(onlineHearingId, statement);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }

    @Test
    public void cannotUploadAStatementIf() {
        when(onlineHearingService.getCcdCase(onlineHearingId)).thenReturn(Optional.empty());

        ResponseEntity responseEntity = statementController.uploadStatement(onlineHearingId, statement);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
        verifyZeroInteractions(storeAppellantStatementService);
    }
}