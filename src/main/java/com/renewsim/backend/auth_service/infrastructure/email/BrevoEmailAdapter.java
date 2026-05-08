package com.renewsim.backend.auth_service.infrastructure.email;

import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Profile({ "docker", "prod" })
public class BrevoEmailAdapter implements EmailPort {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final String frontendUrl;

    public BrevoEmailAdapter(
            @Value("${email.brevo.api-key:}") String apiKey,
            @Value("${email.brevo.sender-email:}") String senderEmail,
            @Value("${email.brevo.sender-name:}") String senderName,
            @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl) {

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("BREVO_API_KEY not configured - BrevoEmailAdapter disabled");
            this.apiKey = null;
            this.senderEmail = null;
            this.senderName = null;
            this.frontendUrl = null;
            this.restTemplate = null;
            return;
        }
        if (senderEmail == null || senderEmail.isBlank()) {
            log.warn("BREVO_SENDER_EMAIL not configured - using default");
            senderEmail = "notifications@renewsim.com";
        }

        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName != null ? senderName : "RenewSim";
        this.frontendUrl = frontendUrl;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void sendVerificationEmail(String toEmail, String username, String verificationToken) {
        if (apiKey == null) {
            log.warn("Brevo not configured - skipping verification email to={}", maskEmail(toEmail));
            return;
        }
        try {
            String link = frontendUrl + "/verify-email?token=" + verificationToken;

            Map<String, Object> payload = buildEmailPayload(
                    toEmail,
                    "Verify your RenewSim email address",
                    buildVerificationHtml(username, link));

            sendEmailViaBrevo(payload);
            log.info("Verification email sent to={}", maskEmail(toEmail));
        } catch (Exception e) {
            log.error("Failed to send verification to={}: {}", maskEmail(toEmail), e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        if (apiKey == null) {
            log.warn("Brevo not configured - skipping password reset email to={}", maskEmail(toEmail));
            return;
        }
        try {
            String link = frontendUrl + "/reset-password?token=" + resetToken;

            Map<String, Object> payload = buildEmailPayload(
                    toEmail,
                    "Reset your RenewSim password",
                    buildPasswordResetHtml(username, link));

            sendEmailViaBrevo(payload);
            log.info("Password reset email sent to={}", maskEmail(toEmail));
        } catch (Exception e) {
            log.error("Failed to send password reset to={}: {}", maskEmail(toEmail), e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Centralized method to send email via Brevo API.
     */
    private void sendEmailViaBrevo(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(
                "https://api.brevo.com/v3/smtp/email",
                request,
                String.class);
    }

    /**
     * Build standard email payload structure.
     */
    private Map<String, Object> buildEmailPayload(String toEmail, String subject, String htmlContent) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", Map.of("email", senderEmail, "name", senderName));
        payload.put("to", new Object[] { Map.of("email", toEmail) });
        payload.put("subject", subject);
        payload.put("htmlContent", htmlContent);
        return payload;
    }

    private String buildVerificationHtml(String username, String link) {
        return String.format(
                """
                        <!DOCTYPE html><html><head><meta charset="UTF-8"></head><body style="font-family:Arial,sans-serif;padding:20px;">
                        <div style="max-width:600px;margin:0 auto;">
                        <h2 style="color:#2E7D32;">Welcome to RenewSim, %s!</h2>
                        <p>Thank you for registering. Please verify your email address to activate your account:</p>
                        <a href="%s" style="display:inline-block;background:#2E7D32;color:white;padding:15px 30px;text-decoration:none;border-radius:5px;margin:20px 0;">Verify Email</a>
                        <p style="color:#666;margin-top:20px;">Or copy and paste this link:<br><span style="color:#1976D2;">%s</span></p>
                        <p style="color:#666;font-size:14px;">This link expires in 48 hours.</p>
                        <p style="color:#999;font-size:12px;">If you didn't create this account, please ignore this email.</p>
                        </div></body></html>""",
                username, link, link);
    }

    private String buildPasswordResetHtml(String username, String link) {
        return String.format(
                """
                        <!DOCTYPE html><html><head><meta charset="UTF-8"></head><body style="font-family:Arial,sans-serif;padding:20px;">
                        <div style="max-width:600px;margin:0 auto;">
                        <h2 style="color:#2E7D32;">Password Reset Request</h2>
                        <p>Hi %s,</p>
                        <p>We received a request to reset your password. Click the button below to proceed:</p>
                        <a href="%s" style="display:inline-block;background:#2E7D32;color:white;padding:15px 30px;text-decoration:none;border-radius:5px;margin:20px 0;">Reset Password</a>
                        <p style="color:#666;margin-top:20px;">Or copy and paste this link:<br><span style="color:#1976D2;">%s</span></p>
                        <p style="color:#666;font-size:14px;">This link expires in 1 hour.</p>
                        <p style="color:#999;font-size:12px;">If you didn't request this, please ignore this email.</p>
                        </div></body></html>""",
                username, link, link);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@"))
            return "***";
        int at = email.indexOf("@");
        return at <= 2 ? "***" + email.substring(at) : email.substring(0, 2) + "***" + email.substring(at);
    }
}