package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.Pdf;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;

public class HearingRelistedActionTest {

    private CorCcdService corCcdService;
    private IdamTokens idamTokens;
    private Long caseId;
    private String onlineHearingId;
    private HearingRelistedAction underTest;
    private CorEmailService corEmailService;
    private EmailMessageBuilder emailMessageBuilder;

    @Before
    public void setUp() {
        corCcdService = mock(CorCcdService.class);
        IdamService idamService = mock(IdamService.class);
        idamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        caseId = 12345L;
        onlineHearingId = "onlineHearingId";
        corEmailService = mock(CorEmailService.class);

        emailMessageBuilder = mock(EmailMessageBuilder.class);
        underTest = new HearingRelistedAction(mock(StoreOnlineHearingService.class), corCcdService, idamService, corEmailService, emailMessageBuilder);
    }

    @Test
    public void handlesActions() {
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
                        .appeal(Appeal.builder()
                                .hearingType("cor")
                                .build())
                        .build())
                .build();
        Pdf pdf = mock(Pdf.class);
        CohEventActionContext cohEventActionContext = new CohEventActionContext(pdf, sscsCaseDetails);
        when(emailMessageBuilder.getRelistedMessage(sscsCaseDetails)).thenReturn("message body");

        CohEventActionContext result = underTest.handle(caseId, onlineHearingId, cohEventActionContext);

        ArgumentMatcher<SscsCaseData> hasOralHearing = data -> data.getAppeal().getHearingType().equals("oral");
        verify(corCcdService).updateCase(
                argThat(hasOralHearing),
                eq(caseId),
                eq("updateHearingType"),
                eq("SSCS - appeal updated event"),
                eq("Update SSCS hearing type"),
                eq(idamTokens));
        verify(corEmailService).sendEmailToDwp("COR: Hearing required", "message body");
        assertThat(result.getPdf(), is(pdf));
        assertThat(result.getDocument().getData().getAppeal().getHearingType(), is("oral"));
    }
}