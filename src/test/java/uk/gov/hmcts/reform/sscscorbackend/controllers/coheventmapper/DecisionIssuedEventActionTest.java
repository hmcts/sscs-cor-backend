package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someStorePdfResult;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingTribunalsViewService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

public class DecisionIssuedEventActionTest {

    private CorEmailService corEmailService;
    private DwpEmailMessageBuilder dwpEmailMessageBuilder;
    private DecisionIssuedEventAction decisionIssuedEventAction;

    @Before
    public void setUp() {
        StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService = mock(StoreOnlineHearingTribunalsViewService.class);
        corEmailService = mock(CorEmailService.class);
        dwpEmailMessageBuilder = mock(DwpEmailMessageBuilder.class);

        decisionIssuedEventAction = new DecisionIssuedEventAction(
                storeOnlineHearingTribunalsViewService,
                corEmailService,
                dwpEmailMessageBuilder
        );
    }

    @Test
    public void canHandleEvent() {
        String message = "someMessage";
        StorePdfResult storePdfResult = someStorePdfResult();
        when(dwpEmailMessageBuilder.getDecisionIssuedMessage(storePdfResult.getDocument())).thenReturn(message);

        long caseId = 123L;
        String onlineHearingId = "onlineHearingId";

        decisionIssuedEventAction.handle(caseId, onlineHearingId, storePdfResult);

        String subject = "Preliminary view offered (" + storePdfResult.getDocument().getData().getCaseReference() + ")";
        verify(corEmailService).sendPdf(storePdfResult, subject, message);
    }
}