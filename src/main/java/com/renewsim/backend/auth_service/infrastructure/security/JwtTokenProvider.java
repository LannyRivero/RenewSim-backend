package com.renewsim.backend.auth_service.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.config.SecurityJwtProperties;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.renewsim.backend.auth_service.infrastructure.security.JwtClaimUtils.toStringSet;

@Component
public final class JwtTokenProvider implements TokenProvider {

    private final SecurityJwtProperties props;
    private final Clock clock;
    private final Key key;
    private final ObjectMapper objectMapper = new ObjectMapper();

    JwtTokenProvider(SecurityJwtProperties props, Clock clock) {
        this.props = Objects.requireNonNull(props, "SecurityJwtProperties is required");
        this.clock = (clock != null) ? clock : Clock.systemUTC();
        this.key = resolveKey(props);
    }

    @Override
    public String generate(AuthenticatedUser user) {
        Objects.requireNonNull(user, "user must not be null");

        Instant now = Instant.now(clock);
        long nbfSkew = props.nbfSkewOrZero();
        long expSecs = props.expirationSeconds();

        Instant nbf = now.plusSeconds(nbfSkew);
        Instant exp = now.plusSeconds(expSecs);

        Map<String, Object> claims = new HashMap<>(4);
        if (user.roles() != null && !user.roles().isEmpty()) {
            Set<String> normalizedRoles = user.roles().stream()
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .collect(Collectors.toSet());
            claims.put("roles", normalizedRoles);
        }

        if (user.scopes() != null && !user.scopes().isEmpty()) {
            claims.put("scopes", Set.copyOf(user.scopes()));
        }

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuer(props.issuer())
                .setAudience(props.audience())
                .setSubject(user.username())
                .setIssuedAt(Date.from(now))
                .setNotBefore(Date.from(nbf))
                .setExpiration(Date.from(exp))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public Optional<AuthenticatedUser> validate(String token) {
        if (token == null || token.isBlank())
            return Optional.empty();

        try {
            JwtParserBuilder builder = Jwts.parserBuilder()
                    .requireIssuer(props.issuer())
                    .requireAudience(props.audience())
                    .setSigningKey(key)
                    .setClock(() -> Date.from(Instant.now(clock)));

            long skew = props.clockSkewOrZero();
            if (skew > 0)
                builder.setAllowedClockSkewSeconds(skew);

            Jws<Claims> jws = builder.build().parseClaimsJws(token);

            JwsHeader<?> header = jws.getHeader();
            if (!SignatureAlgorithm.HS512.getValue().equals(header.getAlgorithm())) {
                return Optional.empty();
            }

            Claims c = jws.getBody();
            String subject = c.getSubject();
            if (subject == null || subject.isBlank())
                return Optional.empty();

            Set<String> roles = toStringSet(c.get("roles"));
            Set<String> scopes = toStringSet(c.get("scopes"));

            return Optional.of(new AuthenticatedUser(subject, roles, scopes));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @Override
    public long expiresInSeconds() {
        return props.expirationSeconds();
    }

    @Override
    public Optional<String> extractJti(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        try {
            Map<?, ?> payload = parsePayloadUnsafe(token);
            Object jti = payload.get("jti");
            return jti != null ? Optional.of(jti.toString()) : Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> extractExpirationEpochSeconds(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        try {
            Map<?, ?> payload = parsePayloadUnsafe(token);
            Object exp = payload.get("exp");
            if (exp == null) return Optional.empty();
            return Optional.of(((Number) exp).longValue());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * Decodes the JWT payload without signature or time validation.
     * Used only for claim extraction (JTI, expiration) — NOT for authentication.
     */
    private Map<?, ?> parsePayloadUnsafe(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT structure");
        byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        return objectMapper.readValue(payload, Map.class);
    }

    public String generateServiceToken(String serviceName, Set<String> scopes) {
        Objects.requireNonNull(serviceName, "serviceName must not be null");

        Instant now = Instant.now(clock);
        Instant exp = now.plusSeconds(props.serviceExpirationSeconds());

        Map<String, Object> claims = new HashMap<>(2);
        claims.put("roles", Set.of("SERVICE_AUTH"));
        if (scopes != null && !scopes.isEmpty()) {
            claims.put("scopes", Set.copyOf(scopes));
        }

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuer(props.issuer())
                .setAudience(props.audience())
                .setSubject(serviceName)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private static Key resolveKey(SecurityJwtProperties props) {
        if (props.hasSecretBase64()) {
            byte[] decoded = Base64.getDecoder().decode(props.secretBase64());
            if (decoded.length < 64) {
                throw new IllegalStateException("Decoded Base64 JWT secret too short (<64 bytes required for HS512).");
            }
            return Keys.hmacShaKeyFor(decoded);
        }
        if (props.hasPlainSecret()) {
            String secret = props.secret();
            if (secret == null) {
                throw new IllegalStateException("JWT secret is null");
            }
            byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 64) {
                throw new IllegalStateException("Plain JWT secret too short (<64 bytes required for HS512).");
            }
            return Keys.hmacShaKeyFor(raw);
        }
        throw new IllegalStateException("No JWT secret configured (secretBase64 or secret required).");
    }
}