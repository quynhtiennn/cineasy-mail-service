package com.quynhtien.cineasymail.controller;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.quynhtien.cineasymail.config.EmailConfig;
import com.quynhtien.cineasymail.enums.RequestTypeEnum;
import com.quynhtien.cineasymail.model.EmailRequest;
import com.quynhtien.cineasymail.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class EmailController {
    EmailConfig emailConfig;
    EmailService emailService;

    @Value("${security.api-key}")
    @NonFinal
    String apiKey;

    @GetMapping("/login")
    public String login() throws Exception {
        log.info("login");
        return "Visit this URL to authorize: <a href=\"" + emailConfig.getAuthorizationUrl() + "\">Login with Microsoft</a>";
    }

    @GetMapping("/authorize")
    public String authorize(@RequestParam("code") String code) throws Exception {
        log.info("Code: {}", code);
        IAuthenticationResult result = emailConfig.acquireToken(code);

        log.info("Access Token: {}", result.accessToken());
        return """
                <h3>Access Token:</h3>
                <textarea rows="8" cols="80">%s</textarea><br><br>
                Acquired access token successfully! You can now use the /send endpoint to send emails.
                """.formatted(result.accessToken());
    }

    @PostMapping("/send-verification-email")
    public ResponseEntity<String> sendVerificationEmail(
            @RequestHeader("API-KEY") String headerKey,
            @RequestBody EmailRequest request) throws Exception {

        if (!apiKey.equals(headerKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Forbidden: Invalid API key");
        }

        emailService.sendMail(request, RequestTypeEnum.VERIFY_EMAIL);
        return ResponseEntity.ok("Email sent successfully.");
    }

    @PostMapping("/send-reset-password-email")
    public ResponseEntity<String> sendResetPasswordEmail(
            @RequestHeader("API-KEY") String headerKey,
            @RequestBody EmailRequest request) throws Exception {

        if (!apiKey.equals(headerKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Forbidden: Invalid API key");
        }

        emailService.sendMail(request, RequestTypeEnum.RESET_PASSWORD);
        return ResponseEntity.ok("Email sent successfully.");
    }
}



