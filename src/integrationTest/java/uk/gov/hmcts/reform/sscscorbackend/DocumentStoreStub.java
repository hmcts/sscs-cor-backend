package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

public class DocumentStoreStub extends BaseStub {
    public DocumentStoreStub(String url) {
        super(url);
    }

    private static final String uploadResponseTemplate = "{" +
            "  \"_embedded\": {" +
            "    \"documents\": [" +
            "      {  \n" +
            "        \"size\":123,\n" +
            "        \"originalDocumentName\":\"fileName.txt\",\n" +
            "        \"createdOn\":1540286899335,\n" +
            "        \"_links\":{  \n" +
            "          \"self\":{  \n" +
            "            \"href\":\"http://example.com/someDocument\"\n" +
            "          },\n" +
            "        }\n" +
            "      }" +
            "    ]" +
            "  }" +
            "}";

    public void stubUploadFile() {
        wireMock.stubFor(post("/documents")
                .willReturn(okJson(uploadResponseTemplate)));
    }
}
