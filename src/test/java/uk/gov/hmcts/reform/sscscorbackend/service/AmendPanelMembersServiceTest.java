package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CaseData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CcdEvent;

public class AmendPanelMembersServiceTest {

    private CorCcdService corCcdService;
    private String disabilityMember;
    private String medicalMember;
    private String oldDisabilityMember;
    private String oldMedicalMember;
    private String caseId;
    private CaseData newCaseData;
    private AmendPanelMembersService amendPanelMembersService;

    @Before
    public void setUp() {
        corCcdService = mock(CorCcdService.class);
        caseId = "123456";
        disabilityMember = "disability";
        medicalMember = "medical";
        oldDisabilityMember = "oldDisability";
        oldMedicalMember = "oldMedical";
        newCaseData = new CaseData("onlineHearingId", "judge", disabilityMember + "|username", medicalMember + "|username");
        amendPanelMembersService = new AmendPanelMembersService(corCcdService);
    }

    @Test
    public void willAddPanelMembersForANewCase() {
        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), null));

        verify(corCcdService).addUserToCase(disabilityMember, Long.valueOf(caseId));
        verify(corCcdService).addUserToCase(medicalMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void willAddPanelMembersForWhenACaseIsAmendedToHavePanelMembers() {
        amendPanelMembersService.amendPanelMembersPermissions(
                new CcdEvent(
                        new CaseDetails(caseId, newCaseData),
                        new CaseDetails(caseId, new CaseData("onlineHearingId", null, null, null))
                ));

        verify(corCcdService).addUserToCase(disabilityMember, Long.valueOf(caseId));
        verify(corCcdService).addUserToCase(medicalMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }


    @Test
    public void willNotAddPanelMembersForAnUncahangedCase() {
        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), new CaseDetails(caseId, newCaseData)));

        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void willAddNewPanelMembersAndRemoveOldOnes() {
        CaseData oldCaseData = new CaseData("onlineHearingId", "oldJudge", oldDisabilityMember + "|username", oldMedicalMember + "|username");
        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), new CaseDetails(caseId, oldCaseData)));

        verify(corCcdService).addUserToCase(disabilityMember, Long.valueOf(caseId));
        verify(corCcdService).addUserToCase(medicalMember, Long.valueOf(caseId));
        verify(corCcdService).removeUserFromCase(oldDisabilityMember, Long.valueOf(caseId));
        verify(corCcdService).removeUserFromCase(oldMedicalMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void willChangeJustTheDisabilityMember() {
        CaseData oldCaseData = new CaseData("onlineHearingId", "oldJudge", oldDisabilityMember + "|username", medicalMember + "|username");
        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), new CaseDetails(caseId, oldCaseData)));

        verify(corCcdService).addUserToCase(disabilityMember, Long.valueOf(caseId));
        verify(corCcdService).removeUserFromCase(oldDisabilityMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void willChangeJustTheMedicalMember() {
        CaseData oldCaseData = new CaseData("onlineHearingId", "oldJudge", disabilityMember + "|username", oldMedicalMember + "|username");
        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), new CaseDetails(caseId, oldCaseData)));

        verify(corCcdService).addUserToCase(medicalMember, Long.valueOf(caseId));
        verify(corCcdService).removeUserFromCase(oldMedicalMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void stripOutUserName() {
        String fixedListValue = "123456|Chris Davidson";
        assertEquals("123456", amendPanelMembersService.stripOutUserName(fixedListValue));
    }

    @Test
    public void stripOutUserNameOldFormat() {
        String fixedListValue = "123456";
        assertEquals("123456", amendPanelMembersService.stripOutUserName(fixedListValue));
    }

    @Test
    public void stripOutUserNameNull() {
        String fixedListValue = null;
        assertEquals(null, amendPanelMembersService.stripOutUserName(fixedListValue));
    }

    @Test
    public void willAddOneMedicalPanelMember() {
        newCaseData = new CaseData("onlineHearingId", "judge", "", medicalMember + "|username");
        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), null));

        verify(corCcdService).addUserToCase(medicalMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void willAddOneDisabilityPanelMember() {
        newCaseData = new CaseData("onlineHearingId", "judge", disabilityMember + "|username", "");
        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), null));

        verify(corCcdService).addUserToCase(disabilityMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void willChangeFromJustOneDisabilityMemberToOneMedicalMember() {
        CaseData oldCaseData = new CaseData("onlineHearingId", "judge", oldDisabilityMember + "|username", "");
        newCaseData = new CaseData("onlineHearingId", "judge", "", medicalMember + "|username");
        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), new CaseDetails(caseId, oldCaseData)));

        verify(corCcdService).addUserToCase(medicalMember, Long.valueOf(caseId));
        verify(corCcdService).removeUserFromCase(oldDisabilityMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void willChangeFromJustOneMedicalMemberToOneDisabilityMember() {
        CaseData oldCaseData = new CaseData("onlineHearingId", "judge", "", oldMedicalMember + "|username");
        newCaseData = new CaseData("onlineHearingId", "judge", disabilityMember + "|username", "");
        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), new CaseDetails(caseId, oldCaseData)));

        verify(corCcdService).addUserToCase(disabilityMember, Long.valueOf(caseId));
        verify(corCcdService).removeUserFromCase(oldMedicalMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void willRemovePanelMembers() {
        CaseData oldCaseData = new CaseData("onlineHearingId", "judge", oldDisabilityMember + "|username", oldMedicalMember + "|username");
        newCaseData = new CaseData("onlineHearingId", "judge", "", "");
        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), new CaseDetails(caseId, oldCaseData)));

        verify(corCcdService).removeUserFromCase(oldDisabilityMember, Long.valueOf(caseId));
        verify(corCcdService).removeUserFromCase(oldMedicalMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void doNotRemoveUnassignedMember() {
        CaseData oldCaseData = new CaseData("onlineHearingId", "unassigned", "unassigned", "unassigned");
        newCaseData = new CaseData("onlineHearingId", "judge", disabilityMember + "|username", medicalMember + "|username");

        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), new CaseDetails(caseId, oldCaseData)));

        verify(corCcdService).addUserToCase(disabilityMember, Long.valueOf(caseId));
        verify(corCcdService).addUserToCase(medicalMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }

    @Test
    public void doNotAddUnassignedMember() {
        CaseData oldCaseData = new CaseData("onlineHearingId", "judge", disabilityMember + "|username", medicalMember + "|username");
        newCaseData = new CaseData("onlineHearingId", "unassigned", "unassigned", "unassigned");

        amendPanelMembersService.amendPanelMembersPermissions(new CcdEvent(new CaseDetails(caseId, newCaseData), new CaseDetails(caseId, oldCaseData)));

        verify(corCcdService).removeUserFromCase(disabilityMember, Long.valueOf(caseId));
        verify(corCcdService).removeUserFromCase(medicalMember, Long.valueOf(caseId));
        verifyNoMoreInteractions(corCcdService);
    }
}