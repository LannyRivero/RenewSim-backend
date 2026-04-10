package com.renewsim.backend.auth_service.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record SecurityJwtProperties(

        // Token issuer (iss) and expected audience (aud)
        @NotBlank String issuer,
        @NotBlank String audience,

        // Option 1: plain text key (>= 32 chars for HS256)
        @Nullable String secret,

        // Option 2: Base64 key (recommended in production)
        @Nullable String secretBase64,

        // exp: token duration in seconds
        @Min(60) long expirationSeconds,

        // Optional nbf skew (seconds). If null, implementation assumes 0.
        @Nullable Long notBeforeSkewSeconds,

        // Clock skew tolerance for iat/nbf/exp (seconds)
        @Nullable Long allowedClockSkewSeconds,

        // exp: service token duration in seconds
        @Min(60) long serviceExpirationSeconds

) {

    /** Is a Base64 key configured? */
    public boolean hasSecretBase64() {
        return secretBase64 != null && !secretBase64.isBlank();
    }

    /** Is a plain text key configured? */
    public boolean hasPlainSecret() {
        return secret != null && !secret.isBlank();
    }

    /** Safe nbf skew (0 when null) */
    public long nbfSkewOrZero() {
        return notBeforeSkewSeconds != null ? notBeforeSkewSeconds : 0L;
    }

    /** Safe clock skew (0 when null) */
    public long clockSkewOrZero() {
        return allowedClockSkewSeconds != null ? allowedClockSkewSeconds : 0L;
    }
}
