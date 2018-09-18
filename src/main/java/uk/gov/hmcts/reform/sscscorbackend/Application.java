package uk.gov.hmcts.reform.sscscorbackend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;

@SpringBootApplication
@EnableCircuitBreaker
@EnableFeignClients(basePackages =
        {
                "uk.gov.hmcts.reform.sscscorbackend.service",
                "uk.gov.hmcts.reform.ccd.client",
                "uk.gov.hmcts.reform.sscs.idam",
                "uk.gov.hmcts.reform.authorisation"
        })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@ComponentScan(basePackages = {"uk.gov.hmcts.reform"})
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
}
