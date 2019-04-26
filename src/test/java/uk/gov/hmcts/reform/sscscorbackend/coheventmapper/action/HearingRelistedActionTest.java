package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.action;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.AdditionalMatchers.and;
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
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions.HearingRelistedAction;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreOnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.Pdf;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;

public class HearingRelistedActionTest {

    private CorCcdService corCcdService;
    private IdamTokens idamTokens;
    private Long caseId;
    private String onlineHearingId;
    private HearingRelistedAction underTest;
    private CorEmailService corEmailService;
    private EmailMessageBuilder emailMessageBuilder;
    private CohService cohService;

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
        cohService = mock(CohService.class);
        underTest = new HearingRelistedAction(mock(StoreOnlineHearingService.class), corCcdService, idamService, corEmailService, emailMessageBuilder, cohService);
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
        String relistingReason = "relisting reason";
        when(cohService.getConversations(onlineHearingId)).thenReturn(DataFixtures.someCohConversations(relistingReason));

        CohEventActionContext result = underTest.handle(caseId, onlineHearingId, cohEventActionContext);

        ArgumentMatcher<SscsCaseData> hasOralHearing = data -> "oral".equals(data.getAppeal().getHearingType());
        ArgumentMatcher<SscsCaseData> hasRelistingReason = data -> relistingReason.equals(data.getRelistingReason());
        verify(corCcdService).updateCase(
                and(argThat(hasOralHearing), argThat(hasRelistingReason)),
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