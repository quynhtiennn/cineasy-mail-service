package com.cineasy.mailservice;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.models.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import com.microsoft.graph.http.IHttpRequest;


import java.util.Arrays;
import java.util.concurrent.CompletableFuture;


@Slf4j
public class GraphHelper {
    public static void sendEmail(String accessToken) {


        IAuthenticationProvider authProvider = requestUrl -> CompletableFuture.completedFuture(accessToken);
        GraphServiceClient<Request> graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(authProvider)
                .buildClient();

        Message message = new Message();
        message.subject = "Hello from Java!";
        ItemBody body = new ItemBody();
        body.contentType = BodyType.TEXT;
        body.content = "This is a test email sent using Microsoft Graph.";
        message.body = body;

        Recipient toRecipient = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = "recipient@example.com"; // Replace with actual recipient
        toRecipient.emailAddress = emailAddress;
        message.toRecipients = Arrays.asList(toRecipient);

        graphClient.me().sendMail(UserSendMailParameterSet.newBuilder()
                        .withMessage(message)
                        .withSaveToSentItems(true)
                        .build())
                .buildRequest()
                .post();

        log.info("Email sent successfully.");
    }

}
