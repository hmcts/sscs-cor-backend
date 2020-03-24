package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.action;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someStorePdfResult;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions.DecisionIssuedEventAction;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions.RemovePanelMembersFeature;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreOnlineHearingTribunalsViewService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;

public class DecisionIssuedEventActionTest {

    private CorEmailService corEmailService;
    private EmailMessageBuilder emailMessageBuilder;
    private DecisionIssuedEventAction decisionIssuedEventAction;
    private StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService;
    private RemovePanelMembersFeature removePanelMembersFeature;

    @Before
    public void setUp() {
        storeOnlineHearingTribunalsViewService = mock(StoreOnlineHearingTribunalsViewService.class);
        corEmailService = mock(CorEmailService.class);
        emailMessageBuilder = mock(EmailMessageBuilder.class);

        removePanelMembersFeature = mock(RemovePanelMembersFeature.class);
        decisionIssuedEventAction = new DecisionIssuedEventAction(
                storeOnlineHearingTribunalsViewService,
                corEmailService,
                emailMessageBuilder,
                removePanelMembersFeature);
    }

    @Test
    public void canHandleEvent() {
        String message = "someMessage";
        CohEventActionContext cohEventActionContext = someStorePdfResult();
        when(emailMessageBuilder.getDecisionIssuedMessage(cohEventActionContext.getDocument())).thenReturn(message);

        long caseId = 123L;
        String onlineHearingId = "onlineHearingId";

        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
                        .caseReference("caseReference")
                        .build())
                .build();
        when(storeOnlineHearingTribunalsViewService.storePdf(caseId, onlineHearingId, new PdfData(caseDetails)))
                .thenReturn(cohEventActionContext);

        CohEventActionContext result = decisionIssuedEventAction.handle(caseId, onlineHearingId, caseDetails);

        String subject = "Preliminary view offered (" + cohEventActionContext.getDocument().getData().getCaseReference() + ")";
        verify(corEmailService).sendFileToDwp(cohEventActionContext, subject, message, caseDetails.getId());

        assertThat(result, is(cohEventActionContext));
    }
}