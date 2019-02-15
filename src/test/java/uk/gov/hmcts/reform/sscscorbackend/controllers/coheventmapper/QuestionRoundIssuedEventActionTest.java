package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreQuestionsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

public class QuestionRoundIssuedEventActionTest {
    private StoreQuestionsPdfService storeQuestionsPdfService;
    private CorEmailService corEmailService;
    private QuestionRoundIssuedEventAction questionRoundIssuedEventAction;
    private Long caseId;
    private String hearingId;
    private DwpEmailMessageBuilder dwpEmailMessageBuilder;

    @Before
    public void setUp() {
        storeQuestionsPdfService = mock(StoreQuestionsPdfService.class);
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
        StorePdfResult storePdfResult = mock(StorePdfResult.class);
        String caseReference = "caseRef";
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
                        .caseReference(caseReference)
                        .build())
                .build();
        when(storePdfResult.getDocument()).thenReturn(sscsCaseDetails);
        when(storeQuestionsPdfService.storePdf(caseId, hearingId)).thenReturn(storePdfResult);
        String message = "message";
        when(dwpEmailMessageBuilder.getQuestionMessage(sscsCaseDetails)).thenReturn(message);

        questionRoundIssuedEventAction.handle(caseId, hearingId);

        verify(corEmailService).sendPdf(storePdfResult, "Questions issued to the appellant (" + caseReference + ")", message);
    }
}