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
    private final CorCcdService ccdService;

    public AmendPanelMembersService(CorCcdService ccdService) {
        this.ccdService = ccdService;
    }

    public void amendPanelMembersPermissions(CcdEvent ccdEvent) {
        CaseDetails newCaseDetails = ccdEvent.getCaseDetails();
        CaseData newCase = newCaseDetails.getCaseData();
        List<String> membersToAddPermissionTo = new ArrayList<>(asList(newCase.getAssignedToDisabilityMember(), newCase.getAssignedToMedicalMember()));
        log.info("PANEL MEMBERS New panel members [" + membersToAddPermissionTo + "]");
        long caseId = Long.parseLong(newCaseDetails.getCaseId());
        if (ccdEvent.getCaseDetailsBefore() != null && ccdEvent.getCaseDetailsBefore().getCaseData() != null) {
            CaseData oldCaseDetails = ccdEvent.getCaseDetailsBefore().getCaseData();
            List<String> membersToRemovePermissionsFrom = new ArrayList<>(asList(oldCaseDetails.getAssignedToDisabilityMember(), oldCaseDetails.getAssignedToMedicalMember()));
            log.info("PANEL MEMBERS  Old panel members [" + membersToRemovePermissionsFrom + "]");
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
}
