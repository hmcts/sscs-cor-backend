package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someStatement;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.email.AppellantStatementEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreAppellantStatementService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.AppellantStatementPdfData;

public class AppellantStatementServiceTest {

    private StoreAppellantStatementService storeAppellantStatementService;
    private OnlineHearingService onlineHearingService;
    private AppellantStatementEmailService appellantStatementEmailService;
    private AppellantStatementService appellantStatementService;
    private String someOnlineHearing;

    @Before
    public void setUp() {
        storeAppellantStatementService = mock(StoreAppellantStatementService.class);
        onlineHearingService = mock(OnlineHearingService.class);
        appellantStatementEmailService = mock(AppellantStatementEmailService.class);

        appellantStatementService = new AppellantStatementService(
                storeAppellantStatementService, onlineHearingService, appellantStatementEmailService
        );
        someOnlineHearing = "someOnlineHearing";
    }

    @Test
    public void cannotFindOnlineHearing() {
        when(onlineHearingService.getCcdCase(someOnlineHearing)).thenReturn(Optional.empty());
        Optional handled = appellantStatementService.handleAppellantStatement(someOnlineHearing, someStatement());

        assertThat(handled, is(Optional.empty()));
    }

    @Test
    public void findsOnlineHearing() {
        long id = 1234L;
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearing)).thenReturn(Optional.of(SscsCaseDetails.builder().id(id).build()));
        when(storeAppellantStatementService.storePdf(eq(id), eq(someOnlineHearing), any(AppellantStatementPdfData.class)))
                .thenReturn(mock(CohEventActionContext.class));
        Optional handled = appellantStatementService.handleAppellantStatement(someOnlineHearing, someStatement());

        assertThat(handled.isPresent(), is(true));
    }

    @Test
    public void generatesAndSavesPdf() {
        long id = 1234L;
        when(onlineHearingService.getCcdCaseByIdentifier(someOnlineHearing)).thenReturn(Optional.of(SscsCaseDetails.builder().id(id).build()));
        CohEventActionContext cohEventActionContext = mock(CohEventActionContext.class);
        when(storeAppellantStatementService.storePdf(eq(id), eq(someOnlineHearing), any(AppellantStatementPdfData.class)))
                .thenReturn(cohEventActionContext);
        Optional handled = appellantStatementService.handleAppellantStatement(someOnlineHearing, someStatement());

        assertThat(handled.isPresent(), is(true));
        verify(appellantStatementEmailService).sendEmail(cohEventActionContext);
    }
}