package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.Panel;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRequest;



@Service
public class OnlineHearingService {
    private final CohClient cohClient;

    public OnlineHearingService(@Autowired CohClient cohClient) {
        this.cohClient = cohClient;
    }

    public String createOnlineHearing(String caseId, Panel panel) {
        List<PanelRequest> panelRequest = convertPanel(panel);

        CreateOnlineHearingRequest createOnlineHearingRequest =
                new CreateOnlineHearingRequest(caseId,
                        panelRequest);

        //assume need to create it
        return cohClient.createOnlineHearing("anything",
                "anything",
                createOnlineHearingRequest);

    }

    protected List<PanelRequest> convertPanel(Panel ccdPanel) {
        List<PanelRequest> panel = new ArrayList<>();

        if (ccdPanel != null) {
            if (ccdPanel.getAssignedTo() != null) {
                //set the judge
                PanelRequest panelRequest = new PanelRequest("judge",
                        ccdPanel.getAssignedTo(), "judge");
                panel.add(panelRequest);
            }
            if (ccdPanel.getMedicalMember() != null) {
                //set the medical member
                PanelRequest panelRequest = new PanelRequest("medical_member",
                        ccdPanel.getMedicalMember(), "medical_member");
                panel.add(panelRequest);
            }
            if (ccdPanel.getDisabilityQualifiedMember() != null) {
                //set the disability qualified member
                PanelRequest panelRequest = new PanelRequest("disability_qualified_member",
                        ccdPanel.getDisabilityQualifiedMember(), "disability_qualified_member");
                panel.add(panelRequest);
            }
        }
        return panel;
    }
}
