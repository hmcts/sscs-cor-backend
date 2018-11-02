package uk.gov.hmcts.reform.sscscorbackend.service.documentmanagement;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

@Service
public class DocumentManagementService {
    private static final String OAUTH2_TOKEN = "oauth2Token";
    private static final String USER_ID = "sscs";
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentManagementClient documentManagementClient;
    private final DocumentUploadClientApi documentUploadClientApi;

    public DocumentManagementService(@Autowired AuthTokenGenerator authTokenGenerator,
                                     @Autowired DocumentManagementClient documentManagementClient,
                                     @Autowired DocumentUploadClientApi documentUploadClientApi) {
        this.authTokenGenerator = authTokenGenerator;
        this.documentManagementClient = documentManagementClient;
        this.documentUploadClientApi = documentUploadClientApi;
    }

    public UploadResponse upload(List<MultipartFile> files) {
        String serviceAuthorization = authTokenGenerator.generate();

        return documentUploadClientApi.upload(OAUTH2_TOKEN, serviceAuthorization, USER_ID, files);
    }

    public void delete(String documentId) {
        documentManagementClient.deleteDocument(OAUTH2_TOKEN, authTokenGenerator.generate(), USER_ID, documentId);
    }
}
