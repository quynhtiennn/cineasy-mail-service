package com.cineasy.mailservice;

import com.microsoft.aad.msal4j.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.*;

@Slf4j
public class AuthHelper {
    public static IAuthenticationResult getAccessToken() throws Exception {
        Properties props = new Properties();
        props.load(AuthHelper.class.getClassLoader().getResourceAsStream("config.properties"));

        String clientId = props.getProperty("clientId");
        String tenantId = props.getProperty("tenantId");
        String clientSecret = props.getProperty("clientSecret");
        String redirectUri = props.getProperty("redirectUri");
        String[] scopes = props.getProperty("scopes").split(" ");

        ConfidentialClientApplication app = ConfidentialClientApplication.builder(
                        clientId,
                        ClientCredentialFactory.createFromSecret(clientSecret))
                .authority("https://login.microsoftonline.com/" + tenantId)
                .build();

        AuthorizationRequestUrlParameters parameters = AuthorizationRequestUrlParameters
                .builder(redirectUri, (Set<String>) Arrays.asList(scopes))
                .build();

        log.info("Open this URL in your browser and sign in:");
        log.info(app.getAuthorizationRequestUrl(parameters).toString());

        log.info("Enter the authorization code: ");
        Scanner scanner = new Scanner(System.in);
        String authCode = scanner.nextLine();

        AuthorizationCodeParameters authParams = AuthorizationCodeParameters
                .builder(authCode, new URI(redirectUri))
                .scopes((Set<String>) Arrays.asList(scopes))
                .build();

        return app.acquireToken(authParams).get();
    }

}
