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

    private final AtomicReference<String> lastVerificationToken = new AtomicReference<>();
    private final AtomicReference<String> lastPasswordResetToken = new AtomicReference<>();
    private final AtomicReference<String> lastRecipient = new AtomicReference<>();
    private final AtomicReference<String> lastUsername = new AtomicReference<>();

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
        lastVerificationToken.set(null);
        lastPasswordResetToken.set(null);
        lastRecipient.set(null);
        lastUsername.set(null);
    }
}