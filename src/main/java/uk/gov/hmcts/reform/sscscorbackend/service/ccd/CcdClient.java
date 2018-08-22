package uk.gov.hmcts.reform.sscscorbackend.service.ccd;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.domain.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.idam.IdamService;
import uk.gov.hmcts.reform.sscscorbackend.service.idam.IdamTokens;

@Service
public class CcdClient {
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamService idamService;

    public CcdClient(@Autowired CoreCaseDataApi coreCaseDataApi, @Autowired IdamService idamService) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.idamService = idamService;
    }

    public List<CaseDetails> findCaseBy(Map<String, String> searchCriteria) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseDetailsList = coreCaseDataApi.searchForCaseworker(
                idamTokens.getIdamOauth2Token(),
                idamTokens.getServiceAuthorization(),
                idamTokens.getUserId(),
                "SSCS", // todo needs to go into a property
                "Benefit", // todo needs to go into a property
                new ImmutableMap.Builder<String, String>()
                        .putAll(searchCriteria)
                        // .put("jurisdiction", "SSCS")
                        .build()
        );

        return caseDetailsList.stream()
                .map(CcdUtil::getCaseDetails)
                .collect(toList());
    }
}
