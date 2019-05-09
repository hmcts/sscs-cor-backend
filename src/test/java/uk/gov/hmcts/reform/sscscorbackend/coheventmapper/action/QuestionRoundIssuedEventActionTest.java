package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.action;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions.QuestionRoundIssuedEventAction;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreQuestionsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;

public class QuestionRoundIssuedEventActionTest {
    private CorEmailService corEmailService;
    private QuestionRoundIssuedEventAction questionRoundIssuedEventAction;
    private Long caseId;
    private String hearingId;
    private EmailMessageBuilder emailMessageBuilder;
    private StoreQuestionsPdfService storeQuestionsPdfService;

    @Before
    public void setUp() {
        storeQuestionsPdfService = mock(StoreQuestionsPdfService.class);
        corEmailService = mock(CorEmailService.class);
        emailMessageBuilder = mock(EmailMessageBuilder.class);
        questionRoundIssuedEventAction = new QuestionRoundIssuedEventAction(
                storeQuestionsPdfService, corEmailService,
                emailMessageBuilder);
        caseId = 123456L;
        hearingId = "someHearingId";
    }

    @Test
    public void sendQuestionsToDwp() {
        CohEventActionContext cohEventActionContext = mock(CohEventActionContext.class);
        String caseReference = "caseRef";
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
                        .caseReference(caseReference)
                        .build())
                .build();
        when(cohEventActionContext.getDocument()).thenReturn(sscsCaseDetails);
        String message = "message";
        when(emailMessageBuilder.getQuestionMessage(sscsCaseDetails)).thenReturn(message);
        when(storeQuestionsPdfService.storePdf(caseId, hearingId, new PdfData(sscsCaseDetails)))
                .thenReturn(cohEventActionContext);

        CohEventActionContext result = questionRoundIssuedEventAction.handle(caseId, hearingId, sscsCaseDetails);

        verify(corEmailService).sendFileToDwp(cohEventActionContext, "Questions issued to the appellant (" + caseReference + ")", message);
        assertThat(result, is(cohEventActionContext));
    }
}