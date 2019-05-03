package uk.gov.hmcts.reform.sscscorbackend.service.email;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence;

public class AppellantStatementEmailServiceTest {

    private CorEmailService corEmailService;
    private EmailMessageBuilder emailMessageBuilder;
    private AppellantStatementEmailService appellantStatementEmailService;

    @Before
    public void setUp() {
        corEmailService = mock(CorEmailService.class);
        emailMessageBuilder = mock(EmailMessageBuilder.class);

        appellantStatementEmailService = new AppellantStatementEmailService(corEmailService, emailMessageBuilder);
    }

    @Test
    public void sendsAppellantStatementEmailToDwp() {
        String caseRef = "caseRef";
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().data(SscsCaseData.builder()
                .caseReference(caseRef)
                .build()).build();
        CohEventActionContext cohEventActionContext = new CohEventActionContext(mock(UploadedEvidence.class), sscsCaseDetails);
        String message = "message body";
        when(emailMessageBuilder.getAppellantStatementMessage(sscsCaseDetails)).thenReturn(message);

        appellantStatementEmailService.sendEmail(cohEventActionContext);

        verify(corEmailService).sendFileToDwp(cohEventActionContext, "COR: Additional evidence submitted (" + caseRef + ")", message);
    }

}