package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.TribunalViewResponse;

public class DecisionEmailServiceTest {

    private CorEmailService corEmailService;
    private EmailMessageBuilder emailMessageBuilder;
    private DecisionEmailService decisionEmailService;
    private String messageBody;
    private String caseReference;
    private SscsCaseDetails caseDetails;
    private JuiUrlGenerator juiUrlGenerator;

    @Before
    public void setUp() {
        corEmailService = mock(CorEmailService.class);
        emailMessageBuilder = mock(EmailMessageBuilder.class);
        juiUrlGenerator = mock(JuiUrlGenerator.class);
        decisionEmailService = new DecisionEmailService(corEmailService, emailMessageBuilder, juiUrlGenerator);
        messageBody = "message body";
        caseReference = "caseReference";
        caseDetails = SscsCaseDetails.builder().data(SscsCaseData.builder().caseReference(caseReference).build()).build();
    }

    @Test
    public void sendsDwpAndCaseworkerEmailWhenDecisionAccepted() {
        SscsCaseDetails caseDetails = SscsCaseDetails.builder().data(SscsCaseData.builder().caseReference(caseReference).build()).build();
        when(emailMessageBuilder.getDecisionAcceptedMessage(caseDetails)).thenReturn(messageBody);
        TribunalViewResponse tribunalViewResponse = new TribunalViewResponse("decision_accepted", "reason");

        decisionEmailService.sendEmail(caseDetails, tribunalViewResponse);

        String subject = "Tribunal view accepted (" + caseReference + ")";
        verify(corEmailService).sendEmailToCaseworker(subject, messageBody);
        verify(corEmailService).sendEmailToDwp(subject, messageBody);
        verifyNoMoreInteractions(corEmailService);
    }

    @Test
    public void sendsCaseworkerEmailWhenDecisionRejected() {
        String juiUrl = "someUrl";
        when(juiUrlGenerator.generateUrl(caseDetails)).thenReturn(juiUrl);
        String reason = "reason";
        TribunalViewResponse tribunalViewResponse = new TribunalViewResponse("decision_rejected", reason);
        when(emailMessageBuilder.getDecisionRejectedMessage(caseDetails, reason, juiUrl)).thenReturn(messageBody);

        decisionEmailService.sendEmail(caseDetails, tribunalViewResponse);

        String subject = "Tribunal view rejected (" + caseReference + ")";
        verify(corEmailService).sendEmailToCaseworker(subject, messageBody);
        verifyNoMoreInteractions(corEmailService);
    }
}