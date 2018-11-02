package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.sscscorbackend.service.documentmanagement.DocumentManagementClient;
import uk.gov.hmcts.reform.sscscorbackend.service.documentmanagement.DocumentManagementService;

public class DocumentManagementServiceTest {

    private AuthTokenGenerator authTokenGenerator;
    private DocumentUploadClientApi documentUploadClientApi;
    private String authToken;
    private List<MultipartFile> files;
    private DocumentManagementService documentManagementService;
    private DocumentManagementClient documentManagementClient;

    @Before
    public void setUp() {
        authToken = "authToken";
        files = singletonList(mock(MultipartFile.class));
        authTokenGenerator = mock(AuthTokenGenerator.class);
        documentManagementClient = mock(DocumentManagementClient.class);
        documentUploadClientApi = mock(DocumentUploadClientApi.class);
        documentManagementService = new DocumentManagementService(authTokenGenerator, documentManagementClient, documentUploadClientApi);
    }

    @Test
    public void passesUploadToDocumentUploadClientApi() {
        when(authTokenGenerator.generate()).thenReturn(authToken);

        documentManagementService.upload(files);

        verify(documentUploadClientApi).upload("oauth2Token", authToken, "sscs", files);
    }

    @Test
    public void delete() {
        String someDocumentId = "someDocumentId";
        String someToken = "someToken";
        when(authTokenGenerator.generate()).thenReturn(someToken);

        documentManagementService.delete(someDocumentId);

        verify(documentManagementClient).deleteDocument("oauth2Token", someToken, "sscs", someDocumentId);
    }
}
