package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.PdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.OldPdfService;

public class StoreQuestionsPdfServiceTest {

    private QuestionService questionService;
    private String onlineHearingId;
    private StoreQuestionsPdfService storeQuestionsPdfService;

    @Before
    public void setUp() {
        questionService = mock(QuestionService.class);
        onlineHearingId = "someOnlineHearingId";
        storeQuestionsPdfService = new StoreQuestionsPdfService(
                mock(OldPdfService.class), "sometemplate", mock(CcdPdfService.class), mock(IdamService.class),
                questionService, mock(EvidenceManagementService.class));
    }

    @Test
    public void getPdfQuestionSummary() {
        QuestionRound questionRound = DataFixtures.someQuestionRound();
        when(questionService.getQuestions(onlineHearingId)).thenReturn(questionRound);
        PdfAppealDetails appealDetails = mock(PdfAppealDetails.class);
        PdfQuestionsSummary pdfQuestionsSummary = storeQuestionsPdfService.getPdfContent(mock(PdfData.class), onlineHearingId, appealDetails);

        assertThat(pdfQuestionsSummary, is(new PdfQuestionsSummary(appealDetails, questionRound.getQuestions())));
    }

    @Test
    public void getPdfQuestionRound() {
        when(questionService.getCurrentQuestionRound(onlineHearingId)).thenReturn(66);
        String documentNamePrefix = storeQuestionsPdfService.documentNamePrefix(mock(SscsCaseDetails.class), onlineHearingId);

        assertThat(documentNamePrefix, is("Issued Questions Round 66 - "));
    }
}