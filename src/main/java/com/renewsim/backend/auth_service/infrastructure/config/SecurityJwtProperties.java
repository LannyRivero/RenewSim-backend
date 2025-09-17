package com.renewsim.backend.auth_service.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record SecurityJwtProperties(

        // Quién emite el token (iss) y para quién va (aud)
        @NotBlank String issuer,
        @NotBlank String audience,

        // Opción 1: clave en texto plano (>= 32 chars para HS256)
        @Nullable String secret,

        // Opción 2: clave en Base64 (recomendada en prod)
        @Nullable String secretBase64,

        // exp: duración del token en segundos
        @Min(60) long expirationSeconds,

        // nbf skew opcional (segundos). Si null, asumimos 0 en la implementación.
        @Nullable Long notBeforeSkewSeconds,

        // tolerancia de reloj para iat/nbf/exp (segundos)
        @Nullable Long allowedClockSkewSeconds
) {

    /** ¿Hay clave Base64 configurada? */
    public boolean hasSecretBase64() {
        return secretBase64 != null && !secretBase64.isBlank();
    }

    /** ¿Hay clave en texto plano configurada? */
    public boolean hasPlainSecret() {
        return secret != null && !secret.isBlank();
    }

    /** nbf skew seguro (0 si es null) */
    public long nbfSkewOrZero() {
        return notBeforeSkewSeconds != null ? notBeforeSkewSeconds : 0L;
    }

    /** clock skew seguro (0 si es null) */
    public long clockSkewOrZero() {
        return allowedClockSkewSeconds != null ? allowedClockSkewSeconds : 0L;
    }
}

