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

        // Option 1: plain text key (>= 64 chars for HS512) - WARNING: NOT for production
        // Recommended: use JWT_SECRET env var with base64-encoded key
        @Nullable String secret,

        // Option 2: Base64 key (recommended production) - set via JWT_SECRET_BASE64 env var
        @Nullable String secretBase64,

        // Option 3: Environment variable (Vault-ready) - set via JWT_SECRET env var
        // When set, takes precedence over secret/secretBase64
        @Nullable String secretEnv,

        // Option 4: Base64 environment variable (Vault-ready) - set via JWT_SECRET_BASE64 env var
        @Nullable String secretBase64Env,

        // exp: token duration in seconds
        @Min(60) long expirationSeconds,

        // Optional nbf skew (seconds). If null, implementation assumes 0.
        @Nullable Long notBeforeSkewSeconds,

        // Clock skew tolerance for iat/nbf/exp (seconds)
        @Nullable Long allowedClockSkewSeconds,

        // exp: service token duration in seconds
        @Min(60) long serviceExpirationSeconds

) {

    /** Resolves the effective secret, with env var taking precedence. */
    public String effectiveSecret() {
        if (secretEnv != null && !secretEnv.isBlank()) {
            return secretEnv;
        }
        return secret;
    }

    /** Resolves the effective Base64 secret, with env var taking precedence. */
    public String effectiveSecretBase64() {
        if (secretBase64Env != null && !secretBase64Env.isBlank()) {
            return secretBase64Env;
        }
        return secretBase64;
    }

    /** Is a Base64 key configured? (env takes precedence) */
    public boolean hasSecretBase64() {
        return effectiveSecretBase64() != null && !effectiveSecretBase64().isBlank();
    }

    /** Is a plain text key configured? (env takes precedence) */
    public boolean hasPlainSecret() {
        return effectiveSecret() != null && !effectiveSecret().isBlank();
    }

    /** Returns the effective secret (either plain or base64 decoded) */
    public String getEffectiveSecret() {
        if (hasSecretBase64()) {
            return effectiveSecretBase64();
        }
        return effectiveSecret();
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
