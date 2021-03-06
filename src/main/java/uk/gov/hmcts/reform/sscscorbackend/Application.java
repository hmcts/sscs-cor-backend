package uk.gov.hmcts.reform.sscscorbackend;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.docmosis.service.DocmosisPdfGenerationService;

@SpringBootApplication
@EnableCircuitBreaker
@EnableFeignClients(basePackages =
        {
                "uk.gov.hmcts.reform.ccd.client",
                "uk.gov.hmcts.reform.sscs.idam",
                "uk.gov.hmcts.reform.authorisation",
                "uk.gov.hmcts.reform.sscs.document",
                "uk.gov.hmcts.reform.sscscorbackend.thirdparty"
        })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@ComponentScan(basePackages = {"uk.gov.hmcts.reform"})
@EnableScheduling
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CcdRequestDetails getRequestDetails(@Value("${core_case_data.jurisdictionId}") String coreCaseDataJurisdictionId,
                                               @Value("${core_case_data.caseTypeId}") String coreCaseDataCaseTypeId) {
        return CcdRequestDetails.builder()
                .caseTypeId(coreCaseDataCaseTypeId)
                .jurisdictionId(coreCaseDataJurisdictionId)
                .build();
    }

    @Bean
    public AuthTokenGenerator serviceAuthTokenGenerator(
            @Value("${idam.s2s-auth.totp_secret}") final String secret,
            @Value("${idam.s2s-auth.microservice}") final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microService, serviceAuthorisationApi);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Value("${appeal.email.host}")
    private String emailHost;

    @Value("${appeal.email.port}")
    private int emailPort;

    @Value("${appeal.email.smtp.tls.enabled}")
    private String smtpTlsEnabled;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(emailHost);
        javaMailSender.setPort(emailPort);
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol","smtp");
        properties.setProperty("mail.smtp.starttls.enable", smtpTlsEnabled);
        properties.put("mail.smtp.ssl.trust","*");
        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }

    @Bean
    public HttpClient serviceTokenParserHttpClient() {
        String proxyHost = System.getProperty("http.proxyHost");
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder = httpClientBuilder.setUserAgent("christest");
        if (proxyHost != null) {
            Integer proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
            httpClientBuilder = httpClientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
        }
        return httpClientBuilder.build();
    }

    @Bean
    public HttpClient userTokenParserHttpClient() {
        return serviceTokenParserHttpClient();
    }

    @Bean
    public DocmosisPdfGenerationService docmosisPdfGenerationService(
            @Value("${docmosis.uri}") String docmosisServiceEndpoint,
            @Value("${docmosis.accessKey}") String docmosisServiceAccessKey,
            RestTemplate restTemplate
    ) {
        return new DocmosisPdfGenerationService(docmosisServiceEndpoint, docmosisServiceAccessKey, restTemplate);
    }

    @Bean
    public OkHttpClient okHttpClient() {
        int timeout = 10;
        return new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MINUTES)
                .readTimeout(timeout, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build();
    }
}
