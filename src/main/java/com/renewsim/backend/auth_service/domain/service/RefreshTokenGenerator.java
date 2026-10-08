package com.renewsim.backend.auth_service.domain.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * Generates opaque refresh tokens for cookie-based refresh-token rotation.
 */
public final class RefreshTokenGenerator {

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private RefreshTokenGenerator() {
    }

    public static String generate() {
        return generate(SECURE_RANDOM);
    }

    static String generate(SecureRandom secureRandom) {
        Objects.requireNonNull(secureRandom, "secureRandom cannot be null");
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
