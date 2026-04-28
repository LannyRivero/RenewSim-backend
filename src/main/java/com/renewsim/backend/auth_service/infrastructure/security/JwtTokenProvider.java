package com.renewsim.backend.auth_service.infrastructure.security;

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
    private final JwtClaimsExtractor claimsExtractor;

    JwtTokenProvider(SecurityJwtProperties props, Clock clock, JwtClaimsExtractor claimsExtractor) {
        this.props = Objects.requireNonNull(props, "SecurityJwtProperties is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
        this.key = resolveKey(props);
        this.claimsExtractor = Objects.requireNonNull(claimsExtractor, "claimsExtractor is required");
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
        return Optional.ofNullable(claimsExtractor.extractJti(token));
    }

    @Override
    public Optional<Long> extractExpirationEpochSeconds(String token) {
        return Optional.ofNullable(claimsExtractor.extractExpiration(token));
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
        String envSecret = props.effectiveSecret();
        String envSecretBase64 = props.effectiveSecretBase64();

        if (envSecretBase64 != null && !envSecretBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(envSecretBase64);
            if (decoded.length < 64) {
                throw new IllegalStateException("Decoded Base64 JWT secret too short (<64 bytes required for HS512).");
            }
            return Keys.hmacShaKeyFor(decoded);
        }
        if (envSecret != null && !envSecret.isBlank()) {
            byte[] raw = envSecret.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 64) {
                throw new IllegalStateException("Plain JWT secret too short (<64 bytes required for HS512).");
            }
            return Keys.hmacShaKeyFor(raw);
        }
        throw new IllegalStateException("No JWT secret configured (JWT_SECRET or JWT_SECRET_BASE64 env var required).");
    }
}