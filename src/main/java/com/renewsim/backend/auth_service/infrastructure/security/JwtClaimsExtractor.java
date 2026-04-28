package com.renewsim.backend.auth_service.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Utility class for extracting claims from JWT tokens without validation.
 * Used only for claim extraction (JTI, expiration) — NOT for authentication.
 */
@Component
public final class JwtClaimsExtractor {

    private final ObjectMapper objectMapper;

    public JwtClaimsExtractor() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Extracts the JTI (JWT ID) claim from the token payload.
     */
    public String extractJti(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            Map<?, ?> payload = parsePayloadUnsafe(token);
            Object jti = payload.get("jti");
            return jti != null ? jti.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts the expiration timestamp (epoch seconds) from the token payload.
     */
    public Long extractExpiration(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            Map<?, ?> payload = parsePayloadUnsafe(token);
            Object exp = payload.get("exp");
            if (exp == null) return null;
            return ((Number) exp).longValue();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decodes the JWT payload without signature or time validation.
     */
    private Map<?, ?> parsePayloadUnsafe(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT structure");
        byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        return objectMapper.readValue(payload, Map.class);
    }
}