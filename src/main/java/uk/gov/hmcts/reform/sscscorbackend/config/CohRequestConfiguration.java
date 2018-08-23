package uk.gov.hmcts.reform.sscscorbackend.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Configuration
@Lazy
public class CohRequestConfiguration {
    @Bean
    public RequestInterceptor getRequestInterceptor(AuthTokenGenerator authTokenGenerator) {
        return template -> template.header("ServiceAuthorization", authTokenGenerator.generate())
                .header(HttpHeaders.AUTHORIZATION, "oauth2Token");
    }
}
