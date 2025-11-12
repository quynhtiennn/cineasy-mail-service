package com.quynhtien.cineasymail.service;

import com.quynhtien.cineasymail.config.EmailConfig;
import com.quynhtien.cineasymail.enums.RequestTypeEnum;
import com.quynhtien.cineasymail.model.EmailRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class EmailService {

    EmailConfig emailConfig;

    @Value("${app.frontend-url}")
    @NonFinal
    String FRONTEND_URL;

    @Value("${app.image-path}")
    @NonFinal
    String IMAGE_PATH;

    static String GRAPH_URL = "https://graph.microsoft.com/v1.0/me/sendMail";

    private String escapeJson(String str) {
        return str.replace("\"", "\\\"")
                .replace("\n", "")
                .replace("\r", "");
    }


    public void sendMail(EmailRequest request, RequestTypeEnum requestType) throws Exception {
        String accessToken = emailConfig.getValidAccessToken();

        String htmlContent;
        String subject;

        String url;
        if (requestType == RequestTypeEnum.VERIFY_EMAIL) {
            url = FRONTEND_URL + "/verify-email?token=" + request.getTokenId();
            subject = "Cineasy: Verify Your Email";
            String verifyEmailContent = """
                     <html>
                      <body style="font-family: Arial, sans-serif; color: #333; background-color: #f9f9f9; padding: 40px; text-align: center;">
                        <div style="max-width: 600px; margin: auto; background-color: #ffffff; border: 1px solid #ddd; border-radius: 10px; padding: 30px; box-shadow: 0 0 10px rgba(0,0,0,0.05);">
                          <h2 style="margin-bottom: 20px;">Welcome to Cineasy 🎬</h2>
                          <p style="margin-bottom: 25px; font-size: 16px;">Please confirm your email by clicking the button below:</p>
                          <a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #000000; color: #ffffff; text-decoration: none; border-radius: 6px; font-weight: bold;">Verify Email</a>
                          <br/><br/>
                          <img src="%s" alt="Cineasy Logo" style="max-width: 500px; width: 100%%; height: auto; margin-top: 25px; border-radius: 6px;"/>
                        </div>
                      </body>
                    </html>
                    """;
            htmlContent = verifyEmailContent.formatted(url, IMAGE_PATH);
        } else if (requestType == RequestTypeEnum.RESET_PASSWORD) {
            url = FRONTEND_URL + "/reset-password?token=" + request.getTokenId();
            subject = "Cineasy: Reset your password";
            String resetPasswordContent = """
                    <html>
                      <body style="font-family: Arial, sans-serif; color: #333; background-color: #f9f9f9; padding: 40px; text-align: center;">
                        <div style="max-width: 600px; margin: auto; background-color: #ffffff; border: 1px solid #ddd; border-radius: 10px; padding: 30px; box-shadow: 0 0 10px rgba(0,0,0,0.05);">
                          <h2 style="margin-bottom: 20px;">Welcome back to Cineasy 🎬</h2>
                          <p style="margin-bottom: 25px; font-size: 16px;">Click below to reset your password 🔐:</p>
                          <a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #000000; color: #ffffff; text-decoration: none; border-radius: 6px; font-weight: bold;">Reset Password</a>
                          <br/><br/>
                          <img src="%s" alt="Cineasy Logo" style="max-width: 500px; width: 100%%; height: auto; margin-top: 25px; border-radius: 6px;"/>
                        </div>
                      </body>
                    </html>
                    """;
            htmlContent = resetPasswordContent.formatted(url, IMAGE_PATH);
        } else {
            throw new IllegalArgumentException("Unknown request type");
        }

        String jsonBody = """
                {
                  "message": {
                    "subject": "%s",
                    "body": {
                      "contentType": "HTML",
                      "content": "%s"
                    },
                    "toRecipients": [
                      {
                        "emailAddress": {
                          "address": "%s"
                        }
                      }
                    ]
                  }
                }
                """.formatted(subject, escapeJson(htmlContent), request.getRecipient());

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(GRAPH_URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 202) {
            throw new RuntimeException("Failed to send email, HTTP code: " + response.statusCode() +
                                       ", body: " + response.body());
        }

    }


}
