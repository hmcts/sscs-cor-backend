package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someStorePdfResult;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingTribunalsViewService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

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
        CohEventActionContext cohEventActionContext = someStorePdfResult();
        when(dwpEmailMessageBuilder.getDecisionIssuedMessage(cohEventActionContext.getDocument())).thenReturn(message);

        long caseId = 123L;
        String onlineHearingId = "onlineHearingId";

        CohEventActionContext result = decisionIssuedEventAction.handle(caseId, onlineHearingId, cohEventActionContext);

        String subject = "Preliminary view offered (" + cohEventActionContext.getDocument().getData().getCaseReference() + ")";
        verify(corEmailService).sendPdfToDwp(cohEventActionContext, subject, message);
        assertThat(result, is(cohEventActionContext));
    }
}