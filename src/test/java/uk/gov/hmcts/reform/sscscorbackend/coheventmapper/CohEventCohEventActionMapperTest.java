package uk.gov.hmcts.reform.sscscorbackend.coheventmapper;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohEvent;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions.CohEventAction;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;

public class CohEventCohEventActionMapperTest {

    private CohEventActionMapper cohEventActionMapper;
    private CohEventAction action;
    private String caseId;
    private String hearingId;
    private List<CohEventAction> actions;
    private CohEventActionRunner cohEventActionRunner;

    @Before
    public void setUp() {
        caseId = "1234";
        hearingId = "hearingId";
        action = mock(CohEventAction.class);
        when(action.cohEvent()).thenReturn("someMappedEvent");
        actions = singletonList(action);
        cohEventActionRunner = mock(CohEventActionRunner.class);
        cohEventActionMapper = new CohEventActionMapper(actions, cohEventActionRunner, true);
    }

    @Test
    public void handlesEventSynchronously() {
        cohEventActionMapper = new CohEventActionMapper(actions, cohEventActionRunner, false);
        when(action.notifyAppellant()).thenReturn(true);
        CohEvent cohEvent = someCohEvent(caseId, hearingId, "someMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        verify(cohEventActionRunner).runActionSync(cohEvent, action);
        assertThat(handle, is(true));
    }

    @Test
    public void handlesEventAsynchronously() {
        when(action.notifyAppellant()).thenReturn(true);
        CohEvent cohEvent = someCohEvent(caseId, hearingId, "someMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        verify(cohEventActionRunner).runActionAsync(cohEvent, action);
        assertThat(handle, is(true));
    }

    @Test
    public void cannotHandleEventCallsNoActions() {
        CohEvent cohEvent = someCohEvent(caseId, hearingId, "someUnMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        assertThat(handle, is(false));
        verifyZeroInteractions(cohEventActionRunner);
    }
}