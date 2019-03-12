package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreQuestionsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

public class QuestionRoundIssuedEventActionTest {
    private CorEmailService corEmailService;
    private QuestionRoundIssuedEventAction questionRoundIssuedEventAction;
    private Long caseId;
    private String hearingId;
    private DwpEmailMessageBuilder dwpEmailMessageBuilder;

    @Before
    public void setUp() {
        StoreQuestionsPdfService storeQuestionsPdfService = mock(StoreQuestionsPdfService.class);
        corEmailService = mock(CorEmailService.class);
        dwpEmailMessageBuilder = mock(DwpEmailMessageBuilder.class);
        questionRoundIssuedEventAction = new QuestionRoundIssuedEventAction(
                storeQuestionsPdfService, corEmailService,
                dwpEmailMessageBuilder);
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
        when(dwpEmailMessageBuilder.getQuestionMessage(sscsCaseDetails)).thenReturn(message);

        CohEventActionContext result = questionRoundIssuedEventAction.handle(caseId, hearingId, cohEventActionContext);

        verify(corEmailService).sendPdfToDwp(cohEventActionContext, "Questions issued to the appellant (" + caseReference + ")", message);
        assertThat(result, is(cohEventActionContext));
    }
}