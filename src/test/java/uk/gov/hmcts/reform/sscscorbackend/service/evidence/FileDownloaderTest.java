package uk.gov.hmcts.reform.sscscorbackend.service.evidence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.UploadedEvidence;

public class FileDownloaderTest {

    private DocumentDownloadClientApi documentDownloadClientApi;
    private AuthTokenGenerator tokenGenerator;
    private FileDownloader fileDownloader;
    private String token;

    @Before
    public void setUp() {
        documentDownloadClientApi = mock(DocumentDownloadClientApi.class);
        tokenGenerator = mock(AuthTokenGenerator.class);

        fileDownloader = new FileDownloader(documentDownloadClientApi, tokenGenerator, "http://somedomain/");

        token = "someToken";
        when(tokenGenerator.generate()).thenReturn(token);

    }

    @Test
    public void canDownloadFile() {
        Resource expectedResource = mock(Resource.class);

        HttpHeaders headers = new HttpHeaders();
        String filename = "filename";
        headers.add("originalfilename", filename);
        String contentType = "application/pdf";
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);

        when(documentDownloadClientApi.downloadBinary("oauth2Token", token, "", "sscs", "/documents/someDocId/binary"))
                .thenReturn(expectedResponse);

        String urlString = "http://somedomain/documents/someDocId/binary";
        UploadedEvidence downloadFile = fileDownloader.downloadFile(urlString);

        assertThat(downloadFile, is(new UploadedEvidence(expectedResource, filename, contentType)));
    }

    @Test(expected = IllegalStateException.class)
    public void cannotDownloadFile() {
        String urlString = "http://somedomain/documents/someDocId/binary";
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        when(documentDownloadClientApi.downloadBinary("oauth2Token", token, "", "sscs", "/documents/someDocId/binary"))
                .thenReturn(expectedResponse);

        fileDownloader.downloadFile(urlString);
    }
}