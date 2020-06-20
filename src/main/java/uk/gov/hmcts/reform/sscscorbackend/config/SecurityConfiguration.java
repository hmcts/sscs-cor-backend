package uk.gov.hmcts.reform.sscscorbackend.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final OncePerRequestFilter oncePerRequestFilter;

    @Autowired
    public SecurityConfiguration(ServiceAuthFilter oncePerRequestFilter) {
        this.oncePerRequestFilter = oncePerRequestFilter;
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().anyRequest();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.addFilterBefore(oncePerRequestFilter, FilterSecurityInterceptor.class)
                .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .authorizeRequests()
                .antMatchers("/health").permitAll()
                .antMatchers("/health/liveness").permitAll()
                .antMatchers("/loggers/**").permitAll()
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/v2/api-docs").permitAll()
                .antMatchers("/case").permitAll()
                .antMatchers("/case/**").permitAll()
                .antMatchers("/citizen/**").authenticated()
                .antMatchers("/continuous-online-hearings/**").authenticated();
    }
}
