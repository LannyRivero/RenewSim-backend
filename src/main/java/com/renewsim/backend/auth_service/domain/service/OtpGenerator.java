package com.renewsim.backend.auth_service.domain.service;

import java.security.SecureRandom;

/**
 * Domain service for generating and hashing OTP codes.
 * Uses SecureRandom for cryptographically secure random generation.
 * No Spring dependencies — pure domain logic.
 */
public class OtpGenerator {

    private static final int OTP_LENGTH = 6;
    private static final int BOUND = 1_000_000; // 10^6 → ensures exactly 6 digits

    private final SecureRandom secureRandom;

    public OtpGenerator() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generates a cryptographically secure 6-digit OTP code.
     * Always returns exactly 6 characters, zero-padded if necessary.
     */
    public String generate() {
        int code = secureRandom.nextInt(BOUND);
        return String.format("%0" + OTP_LENGTH + "d", code);
    }
}