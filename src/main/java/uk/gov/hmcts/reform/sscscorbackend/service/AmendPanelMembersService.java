package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CaseData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CcdEvent;

@Service
public class AmendPanelMembersService {
    private final CorCcdService ccdService;

    public AmendPanelMembersService(CorCcdService ccdService) {
        this.ccdService = ccdService;
    }

    public void amendPanelMembersPermissions(CcdEvent ccdEvent) {
        CaseDetails newCaseDetails = ccdEvent.getCaseDetails();
        CaseData newCase = newCaseDetails.getCaseData();
        List<String> membersToAddPermissionTo = new ArrayList<>(asList(
                stripOutUserName(newCase.getAssignedToDisabilityMember()),
                stripOutUserName(newCase.getAssignedToMedicalMember())));
        long caseId = Long.parseLong(newCaseDetails.getCaseId());
        if (ccdEvent.getCaseDetailsBefore() != null && ccdEvent.getCaseDetailsBefore().getCaseData() != null) {
            CaseData oldCaseDetails = ccdEvent.getCaseDetailsBefore().getCaseData();
            List<String> membersToRemovePermissionsFrom = new ArrayList<>(asList(
                    stripOutUserName(oldCaseDetails.getAssignedToDisabilityMember()),
                    stripOutUserName(oldCaseDetails.getAssignedToMedicalMember())));
            membersToRemovePermissionsFrom.removeAll(membersToAddPermissionTo);
            membersToAddPermissionTo.remove(oldCaseDetails.getAssignedToDisabilityMember());
            membersToAddPermissionTo.remove(oldCaseDetails.getAssignedToMedicalMember());

            for (String member : membersToRemovePermissionsFrom) {
                ccdService.removeUserFromCase(member, caseId);
            }
        }
        for (String member : membersToAddPermissionTo) {
            ccdService.addUserToCase(member, caseId);
        }
    }

    public String stripOutUserName(String fixedListValue) {
        if (fixedListValue != null && fixedListValue.contains("|")) {
            return fixedListValue.substring(0, fixedListValue.indexOf('|'));
        }
        return fixedListValue;
    }
}