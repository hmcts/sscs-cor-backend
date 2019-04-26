package uk.gov.hmcts.reform.sscscorbackend.coheventmapper;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions.CohEventAction;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;

@Slf4j
@Service
public class CohEventActionMapper {
    private final List<CohEventAction> actions;
    private final CohEventActionRunner cohEventActionRunner;
    private final Boolean enableCohEventThreadPool;

    @Autowired
    public CohEventActionMapper(List<CohEventAction> actions,
                                CohEventActionRunner cohEventActionRunner,
                                @Value("${enable_coh_event_thread_pool}") Boolean enableCohEventThreadPool) {
        this.actions = actions;
        this.cohEventActionRunner = cohEventActionRunner;
        this.enableCohEventThreadPool = enableCohEventThreadPool;
    }

    private CohEventAction getActionFor(CohEvent event) {
        return actions.stream()
                .filter(action -> action.cohEvent().equals(event.getEventType()))
                .findFirst()
                .orElse(null);
    }

    public boolean handle(CohEvent event) {
        CohEventAction cohEventAction = getActionFor(event);
        if (cohEventAction != null) {
            if (enableCohEventThreadPool) {
                cohEventActionRunner.runActionAsync(event, cohEventAction);
            } else {
                cohEventActionRunner.runActionSync(event, cohEventAction);
            }
            log.info("Carrying on for [" + event.getCaseId() + "]");
            return true;
        }
        return false;
    }

}
