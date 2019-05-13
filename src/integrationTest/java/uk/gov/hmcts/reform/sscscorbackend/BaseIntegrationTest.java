package uk.gov.hmcts.reform.sscscorbackend;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscscorbackend.stubs.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "appeal.email.port=1026"
})
public abstract class BaseIntegrationTest {

    @Value("${coh.url}")
    private String cohUrl;
    @Value("${idam.s2s-auth.url}")
    private String tokenGeneratorUrl;
    @Value("${idam.url}")
    private String idamUrl;
    @Value("${core_case_data.api.url}")
    private String ccdUrl;
    @Value("${notifications.url}")
    private String notificationsUrl;

    @Value("${idam.oauth2.redirectUrl}")
    private String idamRedirectUrl;
    @Value("${idam.oauth2.client.id}")
    private String clientId;
    @Value("${idam.oauth2.client.secret}")
    private String clientSecret;
    @Value("${document_management.url}")
    private String documentStoreUrl;
    @Value("${pdf.api.url}")
    private String pdfServiceUrl;
    @Value("${appeal.email.port}")
    private int mailPort;

    @LocalServerPort
    protected int applicationPort;

    protected CohStub cohStub;
    protected TokenGeneratorStub tokenGeneratorStub;
    protected CcdStub ccdStub;
    protected IdamStub idamStub;
    protected DocumentStoreStub documentStoreStub;
    protected PdfServiceStub pdfServiceStub;
    protected NotificationsStub notificationsStub;

    private List<BaseStub> stubs = new ArrayList<>();
    protected MailStub mailStub;

    @Before
    public void setUpStubs() throws Exception {
        cohStub = new CohStub(cohUrl);
        stubs.add(cohStub);
        tokenGeneratorStub = new TokenGeneratorStub(tokenGeneratorUrl);
        stubs.add(tokenGeneratorStub);
        ccdStub = new CcdStub(ccdUrl);
        stubs.add(ccdStub);
        idamStub = new IdamStub(idamUrl, idamRedirectUrl, clientId, clientSecret);
        stubs.add(idamStub);
        documentStoreStub = new DocumentStoreStub(documentStoreUrl);
        stubs.add(documentStoreStub);
        notificationsStub = new NotificationsStub(notificationsUrl);
        stubs.add(notificationsStub);
        pdfServiceStub = new PdfServiceStub(pdfServiceUrl);
        stubs.add(pdfServiceStub);
        mailStub = new MailStub(mailPort);
    }

    @After
    public void shutdownStubs() {
        stubs.forEach(BaseStub::shutdown);
        if (mailStub != null) {
            mailStub.stop();
        }
    }

    protected RequestSpecification getRequest() {
        RestAssured.baseURI = "http://localhost:" + applicationPort;
        return RestAssured.given()
                .header("authorization", "Bearer someAuthHeader")
                .header("ServiceAuthorization", "Bearer someServiceAuthHeader");
    }
}
