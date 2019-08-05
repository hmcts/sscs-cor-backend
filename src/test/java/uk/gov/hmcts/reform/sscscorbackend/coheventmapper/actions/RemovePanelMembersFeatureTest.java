package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscscorbackend.service.AmendPanelMembersService.UNASSIGNED_PANEL_MEMBER;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.AmendPanelMembersService;

public class RemovePanelMembersFeatureTest {

    private AmendPanelMembersService amendPanelMembersService;
    private SscsCaseDetails sscsCaseDetails;
    private RemovePanelMembersFeature removePanelMembersFeature;
    private long caseId;

    @Before
    public void setUp() {
        amendPanelMembersService = mock(AmendPanelMembersService.class);
        removePanelMembersFeature = new RemovePanelMembersFeature(amendPanelMembersService);

        caseId = 1234567890L;
        this.sscsCaseDetails = SscsCaseDetails.builder()
                .id(caseId)
                .data(SscsCaseData.builder()
                        .assignedToJudge("judge")
                        .assignedToDisabilityMember("disability")
                        .assignedToMedicalMember("medical")
                        .build())
                .build();
    }

    @Test
    public void removesPanelMemebersFromCcdObject() {
        SscsCaseDetails sscsCaseDetails = removePanelMembersFeature.removePanelMembers(this.sscsCaseDetails);

        SscsCaseData sscsCaseData = sscsCaseDetails.getData();
        assertThat(sscsCaseData.getAssignedToJudge(), is(UNASSIGNED_PANEL_MEMBER));
        assertThat(sscsCaseData.getAssignedToDisabilityMember(), is(UNASSIGNED_PANEL_MEMBER));
        assertThat(sscsCaseData.getAssignedToMedicalMember(), is(UNASSIGNED_PANEL_MEMBER));
    }

    @Test
    public void removedPanelMemberPermissionInCcd() {
        removePanelMembersFeature.removePanelMembers(this.sscsCaseDetails);

        verify(amendPanelMembersService).removePermissionsFrom(caseId, asList("disability", "medical"));
    }

}