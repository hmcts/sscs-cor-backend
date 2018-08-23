package uk.gov.hmcts.reform.sscscorbackend.service;

import static uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRoleCoh.*;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohOnlineHearings;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.Panel;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.CcdClient;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.domain.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRequest;


@Service
public class OnlineHearingService {
    private final CohClient cohClient;
    private final CcdClient ccdClient;

    public OnlineHearingService(@Autowired CohClient cohClient, @Autowired CcdClient ccdClient) {
        this.cohClient = cohClient;
        this.ccdClient = ccdClient;
    }

    public String createOnlineHearing(String caseId, Panel panel) {
        List<PanelRequest> panelRequest = convertPanel(panel);

        CreateOnlineHearingRequest createOnlineHearingRequest =
                new CreateOnlineHearingRequest(caseId,
                        panelRequest);

        //assume need to create it
        return cohClient.createOnlineHearing(createOnlineHearingRequest);

    }

    protected List<PanelRequest> convertPanel(Panel ccdPanel) {
        List<PanelRequest> panel = new ArrayList<>();

        if (ccdPanel != null) {
            if (ccdPanel.getAssignedTo() != null) {
                //set the judge
                PanelRequest panelRequest = new PanelRequest(JUDGE,
                        ccdPanel.getAssignedTo(), JUDGE);
                panel.add(panelRequest);
            }
            if (ccdPanel.getMedicalMember() != null) {
                //set the medical member
                PanelRequest panelRequest = new PanelRequest(MEDICAL_MEMBER,
                        ccdPanel.getMedicalMember(), MEDICAL_MEMBER);
                panel.add(panelRequest);
            }
            if (ccdPanel.getDisabilityQualifiedMember() != null) {
                //set the disability qualified member
                PanelRequest panelRequest = new PanelRequest(DISABILITY_QUALIFIED_MEMBER,
                        ccdPanel.getDisabilityQualifiedMember(), DISABILITY_QUALIFIED_MEMBER);
                panel.add(panelRequest);
            }
        }
        return panel;
    }

    public Optional<OnlineHearing> getOnlineHearing(String emailAddress) {
        List<CaseDetails> cases = ccdClient.findCaseBy(
                ImmutableMap.of("case.subscriptions.appellantSubscription.email", emailAddress)
        );

        return cases.stream()
                .findFirst()
                .flatMap(getHearingFromCoh());
    }

    private Function<CaseDetails, Optional<OnlineHearing>> getHearingFromCoh() {
        return firstCase -> {
            CohOnlineHearings cohOnlineHearings = cohClient.getOnlineHearing(firstCase.getId());

            return cohOnlineHearings.getOnlineHearings().stream()
                    .findFirst()
                    .map(onlineHearing -> new OnlineHearing(onlineHearing.getOnlineHearingId(), null, null));
        };
    }
}
