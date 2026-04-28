package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.auth_service.domain.model.OtpCode;

import java.util.Optional;

/**
 * Output port for OTP code persistence.
 * No framework dependencies — pure domain contract.
 */
public interface OtpCodeRepositoryPort {

    OtpCode save(OtpCode otpCode);

    Optional<OtpCode> findLatestValidByUserId(Long userId, OtpCode.Purpose purpose);

    void invalidateAllByUserId(Long userId, OtpCode.Purpose purpose);

    long countFailedAttempts(Long userId, OtpCode.Purpose purpose);
}