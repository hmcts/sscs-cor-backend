package uk.gov.hmcts.reform.sscscorbackend.controllers;

import java.util.Base64;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscs.idam.Authorize;
import uk.gov.hmcts.reform.sscs.idam.IdamApiClient;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

@Service
@Slf4j
public class IdamServiceTesting {
    public static final int ONE_HOUR = 1000 * 60 * 60;

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamApiClient idamApiClient;

    @Value("${idam.oauth2.user.email}")
    private String idamOauth2UserEmail;

    @Value("${idam.oauth2.user.password}")
    private String idamOauth2UserPassword;

    @Value("${idam.oauth2.client.id}")
    private String idamOauth2ClientId;

    @Value("${idam.oauth2.client.secret}")
    private String idamOauth2ClientSecret;

    @Value("${idam.oauth2.redirectUrl}")
    private String idamOauth2RedirectUrl;

    // Tactical idam token caching solution implemented
    // SSCS-5895 - will deliver the strategic caching solution
    private String cachedToken;

    @Autowired
    IdamServiceTesting(AuthTokenGenerator authTokenGenerator, IdamApiClient idamApiClient) {
        this.authTokenGenerator = authTokenGenerator;
        this.idamApiClient = idamApiClient;
    }

    public String generateServiceAuthorization() {
        return authTokenGenerator.generate();
    }

    @Retryable
    public String getUserId(String oauth2Token) {
        return idamApiClient.getUserDetails(oauth2Token).getId();
    }

    public String getIdamOauth2Token() {

        try {
            log.info("Requesting idam token authorisation email: " + idamOauth2UserEmail);
            log.info("Requesting idam token authorisation pwd: " + idamOauth2UserPassword);
            log.info("Requesting idam token idamOauth2ClientId: " + idamOauth2ClientId);
            log.info("Requesting idam token idamOauth2RedirectUrl: " + idamOauth2RedirectUrl);

            String authorisation = idamOauth2UserEmail + ":" + idamOauth2UserPassword;
            String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());

            Authorize authorize = idamApiClient.authorizeCodeType(
                "Basic " + base64Authorisation,
                "code",
                idamOauth2ClientId,
                idamOauth2RedirectUrl,
                " "
            );

            log.info("Passing authorization code to IDAM to get a token");

            Authorize authorizeToken = idamApiClient.authorizeToken(
                authorize.getCode(),
                "authorization_code",
                idamOauth2RedirectUrl,
                idamOauth2ClientId,
                idamOauth2ClientSecret,
                " "
            );

            cachedToken = "Bearer " + authorizeToken.getAccessToken();

            log.info("Requesting idam token successful");

            return cachedToken;
        } catch (Exception e) {
            log.error("Requesting idam token failed: " + e.getMessage());
            throw e;
        }
    }

    @Scheduled(fixedRate = ONE_HOUR)
    public void evictCacheAtIntervals() {
        log.info("Evicting idam token cache");
        cachedToken = null;
    }

    public IdamTokens getIdamTokens() {

        String idamOauth2Token;

        if (StringUtils.isEmpty(cachedToken)) {
            log.info("No cached IDAM token found, requesting from IDAM service.");
            idamOauth2Token =  getIdamOauth2Token();
        } else {
            log.info("Using cached IDAM token.");
            idamOauth2Token =  cachedToken;
        }

        return IdamTokens.builder()
                .idamOauth2Token(idamOauth2Token)
                .serviceAuthorization(generateServiceAuthorization())
                .userId(getUserId(idamOauth2Token))
                .build();
    }
}
