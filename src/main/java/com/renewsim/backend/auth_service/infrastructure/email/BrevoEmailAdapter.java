package com.renewsim.backend.auth_service.infrastructure.email;

import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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

    public BrevoEmailAdapter(
            @Value("${email.brevo.api-key:}") String apiKey,
            @Value("${email.brevo.sender-email:}") String senderEmail,
            @Value("${email.brevo.sender-name:}") String senderName) {
        
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("BREVO_API_KEY is not configured");
        }
        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalStateException("BREVO_SENDER_EMAIL is not configured");
        }
        
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName != null ? senderName : "RenewSim";
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void sendOtp(String toEmail, String rawOtp, int expiresInSeconds) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("sender", Map.of("email", senderEmail, "name", senderName));
            request.put("to", new Object[] { Map.of("email", toEmail) });
            request.put("subject", "Your RenewSim OTP Code");
            request.put("htmlContent", buildOtpHtml(rawOtp, expiresInSeconds));

            restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email",
                    request,
                    String.class);

            log.info("OTP email sent to={}", maskEmail(toEmail));
        } catch (Exception e) {
            log.error("Failed to send OTP: {}", e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    @Override
    public void sendActivationEmail(String toEmail, String activationToken) {
        try {
            String link = "https://renewsim.example.com/activate?token=" + activationToken;
            
            Map<String, Object> request = new HashMap<>();
            request.put("sender", Map.of("email", senderEmail, "name", senderName));
            request.put("to", new Object[] { Map.of("email", toEmail) });
            request.put("subject", "Activate your RenewSim account");
            request.put("htmlContent", buildActivationHtml(link));

            restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email",
                    request,
                    String.class);

            log.info("Activation email sent to={}", maskEmail(toEmail));
        } catch (Exception e) {
            log.error("Failed to send activation: {}", e.getMessage());
            throw new RuntimeException("Failed to send activation email", e);
        }
    }

    private String buildOtpHtml(String otp, int secs) {
        return String.format("""
            <!DOCTYPE html><html><head><meta charset="UTF-8"></head><body style="font-family:Arial,sans-serif;padding:20px;">
            <div style="max-width:600px;margin:0 auto;">
            <h2 style="color:#2E7D32;">RenewSim</h2>
            <p>Your verification code is:</p>
            <div style="background:#f5f5f5;padding:20px;text-align:center;font-size:32px;font-weight:bold;letter-spacing:8px;">%s</div>
            <p style="color:#666;font-size:14px;">This code expires in %d minutes.</p>
            <p style="color:#999;font-size:12px;">If you didn't request this code, please ignore this email.</p>
            </div></body></html>""", otp, secs / 60);
    }

    private String buildActivationHtml(String link) {
        return String.format("""
            <!DOCTYPE html><html><head><meta charset="UTF-8"></head><body style="font-family:Arial,sans-serif;padding:20px;">
            <div style="max-width:600px;margin:0 auto;">
            <h2 style="color:#2E7D32;">RenewSim</h2>
            <p>Welcome to RenewSim!</p>
            <p>Click the button below to activate your account:</p>
            <a href="%s" style="display:inline-block;background:#2E7D32;color:white;padding:15px 30px;text-decoration:none;border-radius:5px;">Activate Account</a>
            <p style="color:#666;margin-top:20px;">Or copy and paste this link:<br><span style="color:#1976D2;">%s</span></p>
            <p style="color:#999;font-size:12px;">If you didn't create an account, please ignore this email.</p>
            </div></body></html>""", link, link);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf("@");
        return at <= 2 ? "***" + email.substring(at) : email.substring(0, 2) + "***" + email.substring(at);
    }
}