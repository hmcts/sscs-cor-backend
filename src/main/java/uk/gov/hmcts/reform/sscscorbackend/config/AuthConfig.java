package uk.gov.hmcts.reform.sscscorbackend.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;

@Configuration
public class AuthConfig {
    @Bean
    public AuthCheckerServiceAndUserFilter preAuthenticatedProcessingFilter(
            AuthenticationManager authenticationManager,
            @Qualifier("serviceRequestAuthorizer") RequestAuthorizer<Service> serviceRequestAuthorizer,
            RequestAuthorizer<User> userRequestAuthorizer) {
        AuthCheckerServiceAndUserFilter filter
                = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);

        filter.setAuthenticationManager(authenticationManager);

        return filter;
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
        return anyRequest -> Arrays.asList("citizen", "caseworker-sscs");
    }

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
        return request -> Optional.empty();
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return request -> Collections.singletonList("sscs");
    }
}
