package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;

public class EvidenceManagementServiceTest {

    private AuthTokenGenerator authTokenGenerator;
    private DocumentUploadClientApi documentUploadClientApi;
    private String authToken;
    private List<MultipartFile> files;
    private EvidenceManagementService evidenceManagementService;

    @Before
    public void setUp() {
        authTokenGenerator = mock(AuthTokenGenerator.class);
        documentUploadClientApi = mock(DocumentUploadClientApi.class);
        authToken = "authToken";
        files = singletonList(mock(MultipartFile.class));
        evidenceManagementService = new EvidenceManagementService(authTokenGenerator, documentUploadClientApi);
    }

    @Test
    public void passesUploadToDocumentUploadClientApi() {
        when(authTokenGenerator.generate()).thenReturn(authToken);

        evidenceManagementService.upload(files);

        verify(documentUploadClientApi).upload("oauth2Token", authToken, files);
    }
}
