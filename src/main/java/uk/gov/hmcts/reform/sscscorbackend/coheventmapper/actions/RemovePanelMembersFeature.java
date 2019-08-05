package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.sscscorbackend.service.AmendPanelMembersService.UNASSIGNED_PANEL_MEMBER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.AmendPanelMembersService;

@Component
public class RemovePanelMembersFeature {
    private final AmendPanelMembersService amendPanelMembersService;

    @Autowired
    public RemovePanelMembersFeature(AmendPanelMembersService amendPanelMembersService) {
        this.amendPanelMembersService = amendPanelMembersService;
    }

    public SscsCaseDetails removePanelMembers(SscsCaseDetails caseDetails) {
        SscsCaseData sscsCaseData = caseDetails.getData();
        amendPanelMembersService.removePermissionsFrom(
                caseDetails.getId(),
                asList(sscsCaseData.getAssignedToDisabilityMember(), sscsCaseData.getAssignedToMedicalMember())
        );

        sscsCaseData.setAssignedToJudge(UNASSIGNED_PANEL_MEMBER);
        sscsCaseData.setAssignedToDisabilityMember(UNASSIGNED_PANEL_MEMBER);
        sscsCaseData.setAssignedToMedicalMember(UNASSIGNED_PANEL_MEMBER);

        return caseDetails;
    }
}
