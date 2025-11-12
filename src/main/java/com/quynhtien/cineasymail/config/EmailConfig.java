package com.quynhtien.cineasymail.config;

import com.microsoft.aad.msal4j.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collections;

@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Component
public class EmailConfig {
    @Value("${azure.client-id}")
    String CLIENT_ID;

    @Value("${azure.client-secret}")
    String CLIENT_SECRET;

    @Value("${azure.tenant-id}")
    String TENANT_ID;

    @Value("${azure.redirect-uri}")
    String REDIRECT_URI;

    @Value("${azure.scope}")
    String SCOPES;

    String AUTHORITY = "https://login.microsoftonline.com/";


    ConfidentialClientApplication app;
    IAuthenticationResult cachedResult;

    public ConfidentialClientApplication getApp() throws Exception {
        if (app == null) {
            app = ConfidentialClientApplication.builder(
                            CLIENT_ID,
                            ClientCredentialFactory.createFromSecret(CLIENT_SECRET))
                    .authority(AUTHORITY + TENANT_ID)
                    .build();
        }
        return app;
    }

    public String getAuthorizationUrl() throws Exception {
        AuthorizationRequestUrlParameters parameters = AuthorizationRequestUrlParameters
                .builder(REDIRECT_URI, Collections.singleton(SCOPES))
                .responseMode(ResponseMode.QUERY)
                .build();

        return getApp()
                .getAuthorizationRequestUrl(parameters)
                .toString();
    }

    // Acquire initial tokens using auth code
    public IAuthenticationResult acquireToken(String authCode) throws Exception {
        AuthorizationCodeParameters parameters = AuthorizationCodeParameters
                .builder(authCode, new URI(REDIRECT_URI))
                .scopes(Collections.singleton(SCOPES))
                .build();

        IAuthenticationResult result = getApp().acquireToken(parameters).get();
        cachedResult = result;
        return result;
    }

    // Automatically refresh token if expired
    public synchronized String getValidAccessToken(){
        if (cachedResult == null) {
            throw new IllegalStateException("No cached token available. Authorize first.");
        }

        long expiresIn = cachedResult.expiresOnDate().getTime() - System.currentTimeMillis();
        if (expiresIn < 60_000) { // if less than 1 minute remaining
            log.info("Refreshing expired access token...");

            SilentParameters parameters = SilentParameters
                    .builder(Collections.singleton(SCOPES))
                    .account(cachedResult.account())
                    .build();

            try {
                cachedResult = getApp().acquireTokenSilently(parameters).get();
                log.info("Token refreshed successfully.");
            } catch (Exception e) {
                log.warn("Silent token refresh failed: {}", e.getMessage());
                throw new IllegalStateException("Access token expired. Please re-authorize via /login.");
            }
        }

        return cachedResult.accessToken();
    }
}
