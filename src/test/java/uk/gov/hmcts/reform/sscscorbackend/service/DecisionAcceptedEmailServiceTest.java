package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.mockito.Mockito.*;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

public class DecisionAcceptedEmailServiceTest {
    @Test
    public void sendsDwpEmail() {
        CorEmailService corEmailService = mock(CorEmailService.class);
        DwpEmailMessageBuilder dwpEmailMessageBuilder = mock(DwpEmailMessageBuilder.class);
        DecisionAcceptedEmailService decisionAcceptedEmailService = new DecisionAcceptedEmailService(corEmailService, dwpEmailMessageBuilder);

        String messageBody = "message body";
        String caseReference = "caseReference";
        SscsCaseDetails caseDetails = SscsCaseDetails.builder().data(SscsCaseData.builder().caseReference(caseReference).build()).build();
        when(dwpEmailMessageBuilder.getDecisionAcceptedMessage(caseDetails)).thenReturn(messageBody);

        decisionAcceptedEmailService.sendEmail(caseDetails);

        String subject = "Tribunal view accepted (" + caseReference + ")";
        verify(corEmailService).sendEmailToCaseworker(subject, messageBody);
        verify(corEmailService).sendEmailToDwp(subject, messageBody);
    }
}