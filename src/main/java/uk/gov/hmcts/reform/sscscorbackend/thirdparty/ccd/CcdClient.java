package uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.api.CcdAddUser;

@FeignClient(
        name = "Ccd",
        url = "${core_case_data.api.url}"
)
public interface CcdClient {
    @PostMapping(value = "/caseworkers/{userId}/jurisdictions/{jurisdictionId}/case-types/{caseType}/cases/{caseId}/users")
    void addUserToCase(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
            @PathVariable("userId") String userId,
            @PathVariable("jurisdictionId") String jurisdictionId,
            @PathVariable("caseType") String caseType,
            @PathVariable("caseId") long caseId,
            @RequestBody CcdAddUser caseDataContent
    );

    @DeleteMapping(value = "/caseworkers/{userId}/jurisdictions/{jurisdictionId}/case-types/{caseType}/cases/{caseId}/users/{idToDelete}")
    void removeUserFromCase(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
            @PathVariable("userId") String userId,
            @PathVariable("jurisdictionId") String jurisdictionId,
            @PathVariable("caseType") String caseType,
            @PathVariable("caseId") long caseId,
            @PathVariable("idToDelete") String idToDelete
    );
}
