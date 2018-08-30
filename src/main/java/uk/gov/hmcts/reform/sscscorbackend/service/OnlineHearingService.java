package uk.gov.hmcts.reform.sscscorbackend.service;

import static uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRoleCoh.*;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohOnlineHearings;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.Panel;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.CcdClient;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.CcdRequestDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.domain.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.domain.Name;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.PanelRequest;


@Service
public class OnlineHearingService {
    private final CohService cohClient;
    private final CcdClient ccdClient;
    private CcdRequestDetails ccdRequestDetails;

    public OnlineHearingService(@Autowired CohService cohService,
                                @Autowired CcdClient ccdClient,
                                @Autowired CcdRequestDetails ccdRequestDetails
    ) {
        this.cohClient = cohService;
        this.ccdClient = ccdClient;
        this.ccdRequestDetails = ccdRequestDetails;
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
                ccdRequestDetails,
                ImmutableMap.of("case.subscriptions.appellantSubscription.email", emailAddress)
        );

        return cases.stream()
                .filter(caseDetails -> caseDetails.getData().getOnlinePanel() != null)
                .reduce(checkThereIsOnlyOneCase())
                .flatMap(getHearingFromCoh());
    }

    private BinaryOperator<CaseDetails> checkThereIsOnlyOneCase() {
        return (a, b) -> {
            throw new IllegalStateException("Multiple appeals with online hearings found.");
        };
    }

    private Function<CaseDetails, Optional<OnlineHearing>> getHearingFromCoh() {
        return firstCase -> {
            CohOnlineHearings cohOnlineHearings = cohClient.getOnlineHearing(firstCase.getId());

            return cohOnlineHearings.getOnlineHearings().stream()
                    .findFirst()
                    .map(onlineHearing -> {
                        Name name = firstCase.getData().getAppeal().getAppellant().getName();
                        String nameString = name.getFirstName() + " " + name.getLastName();

                        return new OnlineHearing(
                                onlineHearing.getOnlineHearingId(),
                                nameString,
                                firstCase.getData().getCaseReference()
                        );
                    });
        };
    }
}
