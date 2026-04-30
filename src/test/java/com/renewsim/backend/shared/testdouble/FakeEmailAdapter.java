package com.renewsim.backend.shared.testdouble;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.renewsim.backend.auth_service.application.port.out.EmailPort;


@Component
@Primary
@Profile("test")
public class FakeEmailAdapter implements EmailPort {

    private final AtomicReference<String> lastOtpCode = new AtomicReference<>();
    private final AtomicReference<Integer> lastOtpExpiresInSeconds = new AtomicReference<>();
    private final AtomicReference<String> lastActivationToken = new AtomicReference<>();
    private final AtomicReference<String> lastVerificationToken = new AtomicReference<>();
    private final AtomicReference<String> lastPasswordResetToken = new AtomicReference<>();
    private final AtomicReference<String> lastRecipient = new AtomicReference<>();
    private final AtomicReference<String> lastUsername = new AtomicReference<>();

    @Override
    public void sendOtp(String to, String otpCode, int expiresInSeconds) {
        lastRecipient.set(to);
        lastOtpCode.set(otpCode);
        lastOtpExpiresInSeconds.set(expiresInSeconds);
    }

    @Override
    public void sendActivationEmail(String to, String token) {
        lastRecipient.set(to);
        lastActivationToken.set(token);
    }

    @Override
    public void sendVerificationEmail(String to, String username, String verificationToken) {
        lastRecipient.set(to);
        lastUsername.set(username);
        lastVerificationToken.set(verificationToken);
    }

    @Override
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        lastRecipient.set(to);
        lastUsername.set(username);
        lastPasswordResetToken.set(resetToken);
    }

    // Getters
    public String getLastOtpCode() {
        return lastOtpCode.get();
    }

    public Integer getLastOtpExpiresInSeconds() {
        return lastOtpExpiresInSeconds.get();
    }

    public String getLastActivationToken() {
        return lastActivationToken.get();
    }

    public String getLastVerificationToken() {
        return lastVerificationToken.get();
    }

    public String getLastPasswordResetToken() {
        return lastPasswordResetToken.get();
    }

    public String getLastRecipient() {
        return lastRecipient.get();
    }

    public String getLastUsername() {
        return lastUsername.get();
    }

    public void clear() {
        lastOtpCode.set(null);
        lastOtpExpiresInSeconds.set(null);
        lastActivationToken.set(null);
        lastVerificationToken.set(null);
        lastPasswordResetToken.set(null);
        lastRecipient.set(null);
        lastUsername.set(null);
    }
}