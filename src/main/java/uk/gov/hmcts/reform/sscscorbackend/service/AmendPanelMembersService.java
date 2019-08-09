package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CaseData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CcdEvent;

@Slf4j
@Service
public class AmendPanelMembersService {
    public static final String UNASSIGNED_PANEL_MEMBER = "unassigned";
    private final CorCcdService ccdService;

    public AmendPanelMembersService(CorCcdService ccdService) {
        this.ccdService = ccdService;
    }

    public void amendPanelMembersPermissions(CcdEvent ccdEvent) {
        CaseDetails newCaseDetails = ccdEvent.getCaseDetails();
        CaseData newCase = newCaseDetails.getCaseData();
        List<String> membersToAddPermissionTo = new ArrayList<>(asList(
                newCase.getAssignedToDisabilityMember(),
                newCase.getAssignedToMedicalMember()));
        long caseId = Long.parseLong(newCaseDetails.getCaseId());
        if (ccdEvent.getCaseDetailsBefore() != null && ccdEvent.getCaseDetailsBefore().getCaseData() != null) {
            CaseData oldCaseDetails = ccdEvent.getCaseDetailsBefore().getCaseData();
            List<String> membersToRemovePermissionsFrom = new ArrayList<>(asList(
                    oldCaseDetails.getAssignedToDisabilityMember(),
                    oldCaseDetails.getAssignedToMedicalMember()));
            membersToRemovePermissionsFrom.removeAll(membersToAddPermissionTo);
            membersToAddPermissionTo.remove(oldCaseDetails.getAssignedToDisabilityMember());
            membersToAddPermissionTo.remove(oldCaseDetails.getAssignedToMedicalMember());

            removePermissionsFrom(caseId, membersToRemovePermissionsFrom);
        }
        for (String member : membersToAddPermissionTo) {
            if (member != null && member.length() != 0 && !member.equals(UNASSIGNED_PANEL_MEMBER)) {
                log.info("Add member with id starting [" + member.substring(0, 3) + "] to case [" + caseId + "]");
                ccdService.addUserToCase(stripOutUserName(member), caseId);
            }
        }
    }

    public void removePermissionsFrom(long caseId, List<String> membersToRemovePermissionsFrom) {
        for (String member : membersToRemovePermissionsFrom) {
            if (member != null && member.length() != 0 && !member.equals(UNASSIGNED_PANEL_MEMBER)) {
                log.info("Remove member with id starting [" + member.substring(0, 3) + "] from case [" + caseId + "]");
                ccdService.removeUserFromCase(stripOutUserName(member), caseId);
            }
        }
    }

    public String stripOutUserName(String fixedListValue) {
        if (fixedListValue != null && fixedListValue.contains("|")) {
            return fixedListValue.substring(0, fixedListValue.indexOf('|'));
        }
        return fixedListValue;
    }
}
