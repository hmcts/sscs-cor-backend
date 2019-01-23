package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohEvent;

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;

public class CohEventActionMapperTest {

    private CohEventActionMapper cohEventActionMapper;
    private CohEventActionMapper.Action action;

    @Before
    public void setUp() {
        HashMap<String, CohEventActionMapper.Action> actions = new HashMap<>();
        action = mock(CohEventActionMapper.Action.class);
        actions.put("someMappedEvent", action);
        cohEventActionMapper = new CohEventActionMapper(actions);
    }

    @Test
    public void handlesEvent() {
        CohEvent cohEvent = someCohEvent("1234", "hearingId", "someMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        verify(action).handle(1234L, "hearingId", cohEvent);
        assertThat(handle, is(true));
    }

    @Test
    public void cannotHandleEventCallsNoActions() {
        CohEvent cohEvent = someCohEvent("1234", "hearingId", "someUnMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        verifyZeroInteractions(action);
        assertThat(handle, is(false));
    }
}