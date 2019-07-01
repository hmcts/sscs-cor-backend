package uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CreateCcdCaseService;
import uk.gov.hmcts.reform.sscs.ccd.service.ReadCcdCaseService;
import uk.gov.hmcts.reform.sscs.ccd.service.SearchCcdCaseService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.api.CcdAddUser;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.api.CcdHistoryEvent;

public class CorCcdServiceTest {

    private CcdClient ccdClient;
    private IdamService idamService;
    private String authToken;
    private String serviceAuthToken;
    private String userId;
    private String jurisdictionId;
    private String caseTypeId;
    private CcdRequestDetails ccdRequestDetails;
    private CorCcdService corCcdService;
    private long caseId;

    @Before
    public void setUp() throws Exception {
        ccdClient = mock(CcdClient.class);
        idamService = mock(IdamService.class);
        authToken = "authToken";
        serviceAuthToken = "serviceAuthToken";
        userId = "userId";
        when(idamService.getIdamTokens()).thenReturn(
                IdamTokens.builder().idamOauth2Token(authToken).serviceAuthorization(serviceAuthToken).userId(userId).build()
        );
        jurisdictionId = "jurisdictionId";
        caseTypeId = "caseTypeId";
        ccdRequestDetails = CcdRequestDetails.builder()
                .jurisdictionId(jurisdictionId)
                .caseTypeId(caseTypeId)
                .build();
        corCcdService = new CorCcdService(
                mock(CreateCcdCaseService.class),
                mock(SearchCcdCaseService.class),
                mock(UpdateCcdCaseService.class),
                mock(ReadCcdCaseService.class),
                ccdClient,
                idamService,
                ccdRequestDetails,
                mock(CoreCaseDataApi.class)
        );
        caseId = 123L;
    }

    @Test
    public void canAddUserToCase() {
        String userToAdd = "userToAdd";
        corCcdService.addUserToCase(userToAdd, caseId);

        verify(ccdClient).addUserToCase(
                authToken,
                serviceAuthToken,
                userId,
                jurisdictionId,
                caseTypeId,
                caseId,
                new CcdAddUser(userToAdd)
        );
    }

    @Test
    public void canRemoveAUser() {
        String userToRemove = "userToRemove";
        corCcdService.removeUserFromCase(userToRemove, caseId);

        verify(ccdClient).removeUserFromCase(authToken,
                serviceAuthToken,
                userId,
                jurisdictionId,
                caseTypeId,
                caseId,
                userToRemove);
    }

    @Test
    public void canGetHistoryEvents() {
        List<CcdHistoryEvent> historyEvents = asList(new CcdHistoryEvent("id"));
        when(ccdClient.getHistoryEvents(authToken,
                serviceAuthToken,
                userId,
                jurisdictionId,
                caseTypeId,
                caseId)).thenReturn(historyEvents);

        List<CcdHistoryEvent> actualHistoryEvents = corCcdService.getHistoryEvents(caseId);

        assertThat(actualHistoryEvents, is(historyEvents));
    }
}