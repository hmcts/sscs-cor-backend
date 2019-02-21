package uk.gov.hmcts.reform.sscscorbackend;

import java.util.Properties;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;


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
@EnableAsync
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
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("COHEventHandler-");
        executor.initialize();
        return executor;
    }
}
