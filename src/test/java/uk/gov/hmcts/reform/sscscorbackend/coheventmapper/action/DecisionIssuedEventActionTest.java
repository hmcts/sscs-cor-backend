package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.action;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someStorePdfResult;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions.DecisionIssuedEventAction;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingTribunalsViewService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

public class DecisionIssuedEventActionTest {

    private CorEmailService corEmailService;
    private EmailMessageBuilder emailMessageBuilder;
    private DecisionIssuedEventAction decisionIssuedEventAction;

    @Before
    public void setUp() {
        StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService = mock(StoreOnlineHearingTribunalsViewService.class);
        corEmailService = mock(CorEmailService.class);
        emailMessageBuilder = mock(EmailMessageBuilder.class);

        decisionIssuedEventAction = new DecisionIssuedEventAction(
                storeOnlineHearingTribunalsViewService,
                corEmailService,
                emailMessageBuilder
        );
    }

    @Test
    public void canHandleEvent() {
        String message = "someMessage";
        CohEventActionContext cohEventActionContext = someStorePdfResult();
        when(emailMessageBuilder.getDecisionIssuedMessage(cohEventActionContext.getDocument())).thenReturn(message);

        long caseId = 123L;
        String onlineHearingId = "onlineHearingId";

        CohEventActionContext result = decisionIssuedEventAction.handle(caseId, onlineHearingId, cohEventActionContext);

        String subject = "Preliminary view offered (" + cohEventActionContext.getDocument().getData().getCaseReference() + ")";
        verify(corEmailService).sendPdfToDwp(cohEventActionContext, subject, message);
        assertThat(result, is(cohEventActionContext));
    }
}