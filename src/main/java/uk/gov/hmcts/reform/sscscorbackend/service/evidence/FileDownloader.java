package uk.gov.hmcts.reform.sscscorbackend.service.evidence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence;

@Component
public class FileDownloader {
    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final String documentManagementUrl;

    private static final String OAUTH2_TOKEN = "oauth2Token";
    private static final String USER_ID = "sscs";

    public FileDownloader(
            DocumentDownloadClientApi documentDownloadClientApi,
            AuthTokenGenerator authTokenGenerator,
            @Value("${document_management.url}") String documentManagementUrl
    ) {
        this.documentDownloadClientApi = documentDownloadClientApi;
        this.authTokenGenerator = authTokenGenerator;
        this.documentManagementUrl = documentManagementUrl;
    }

    public UploadedEvidence downloadFile(String urlString) {
        ResponseEntity<Resource> response = documentDownloadClientApi.downloadBinary(
                OAUTH2_TOKEN,
                authTokenGenerator.generate(),
                "",
                USER_ID,
                getDownloadUrl(urlString)
        );
        if (HttpStatus.OK.equals(response.getStatusCode())) {
            return new UploadedEvidence(response.getBody(), response.getHeaders().get("originalfilename").get(0), response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        } else {
            throw new IllegalStateException("Cannot download document that is stored in CCD got " +
                    "[" + response.getStatusCode() + "] " + response.getBody());
        }
    }

    private String getDownloadUrl(String urlString) {
        String path = urlString.replace(documentManagementUrl, "");
        if (path.startsWith("/")) {
            return path;
        }

        return "/" + path;
    }
}
