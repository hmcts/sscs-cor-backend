package uk.gov.hmcts.reform.sscscorbackend.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;

public class AuthCheckerServiceAndUserFilter extends uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter {

    private final RequestMatcher matcher;

    public AuthCheckerServiceAndUserFilter(RequestAuthorizer<Service> serviceRequestAuthorizer, RequestAuthorizer<User> userRequestAuthorizer, RequestMatcher matcher) {
        super(serviceRequestAuthorizer, userRequestAuthorizer);
        this.matcher = matcher;
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if (matcher.matches(request)) {
            super.doFilter(req, resp, chain);
        } else {
            chain.doFilter(req, resp);
        }
    }
}
