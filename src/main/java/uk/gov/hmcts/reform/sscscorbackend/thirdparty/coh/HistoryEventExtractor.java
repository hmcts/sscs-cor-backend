package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohState;

public final class HistoryEventExtractor {

    private HistoryEventExtractor() {

    }

    public static Optional<String> getStateDate(List<CohState> history, String state) {
        if (history != null) {
            return history.stream()
                    .filter(historyState -> historyState.getStateName().equals(state))
                    .map(CohState::getStateDateTime)
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }
}
