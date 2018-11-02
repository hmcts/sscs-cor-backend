package uk.gov.hmcts.reform.sscscorbackend.service.documentmanagement;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "documentManagement",
        url = "${document_management.url}"
)
public interface DocumentManagementClient {
    @DeleteMapping(value = "/documents/{documentId}")
    void deleteDocument(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
            @RequestHeader("user-id") String userId,
            @PathVariable("documentId") String documentId);
}
