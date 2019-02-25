package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.TribunalViewResponse;

public class DecisionEmailServiceTest {

    private CorEmailService corEmailService;
    private DwpEmailMessageBuilder dwpEmailMessageBuilder;
    private DecisionEmailService decisionEmailService;
    private String messageBody;
    private String caseReference;
    private SscsCaseDetails caseDetails;

    @Before
    public void setUp() {
        corEmailService = mock(CorEmailService.class);
        dwpEmailMessageBuilder = mock(DwpEmailMessageBuilder.class);
        decisionEmailService = new DecisionEmailService(corEmailService, dwpEmailMessageBuilder);
        messageBody = "message body";
        caseReference = "caseReference";
        caseDetails = SscsCaseDetails.builder().data(SscsCaseData.builder().caseReference(caseReference).build()).build();
    }

    @Test
    public void sendsDwpAndCaseworkerEmailWhenDecisionAccepted() {
        SscsCaseDetails caseDetails = SscsCaseDetails.builder().data(SscsCaseData.builder().caseReference(caseReference).build()).build();
        when(dwpEmailMessageBuilder.getDecisionAcceptedMessage(caseDetails)).thenReturn(messageBody);
        TribunalViewResponse tribunalViewResponse = new TribunalViewResponse("decision_accepted", "reason");

        decisionEmailService.sendEmail(caseDetails, tribunalViewResponse);

        String subject = "Tribunal view accepted (" + caseReference + ")";
        verify(corEmailService).sendEmailToCaseworker(subject, messageBody);
        verify(corEmailService).sendEmailToDwp(subject, messageBody);
        verifyNoMoreInteractions(corEmailService);
    }

    @Test
    public void sendsCaseworkerEmailWhenDecisionRejected() {
        String reason = "reason";
        TribunalViewResponse tribunalViewResponse = new TribunalViewResponse("decision_rejected", reason);
        when(dwpEmailMessageBuilder.getDecisionRejectedMessage(caseDetails, reason)).thenReturn(messageBody);

        decisionEmailService.sendEmail(caseDetails, tribunalViewResponse);

        String subject = "Tribunal view rejected (" + caseReference + ")";
        verify(corEmailService).sendEmailToCaseworker(subject, messageBody);
        verifyNoMoreInteractions(corEmailService);
    }
}