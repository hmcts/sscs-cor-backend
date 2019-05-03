package uk.gov.hmcts.reform.sscscorbackend.service.evidence;

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

    private static final String OAUTH2_TOKEN = "oauth2Token";
    private static final String USER_ID = "sscs";

    public FileDownloader(
            DocumentDownloadClientApi documentDownloadClientApi,
            AuthTokenGenerator authTokenGenerator
    ) {
        this.documentDownloadClientApi = documentDownloadClientApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    public UploadedEvidence downloadFile(String urlString) {
        ResponseEntity<Resource> response = documentDownloadClientApi.downloadBinary(
                OAUTH2_TOKEN,
                authTokenGenerator.generate(),
                "",
                USER_ID,
                "/documents" + urlString.split("documents")[1]
        );
        if (HttpStatus.OK.equals(response.getStatusCode())) {

            return new UploadedEvidence(response.getBody(), response.getHeaders().get("originalfilename").get(0), response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        } else {
            throw new IllegalStateException("Cannot download document that is stored in CCD got " +
                    "[" + response.getStatusCode() + "] " + response.getBody());
        }
    }
}
