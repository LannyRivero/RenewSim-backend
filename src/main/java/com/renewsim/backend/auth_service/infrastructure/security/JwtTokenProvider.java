package com.renewsim.backend.auth_service.infrastructure.security;

import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.config.SecurityJwtProperties;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static com.renewsim.backend.auth_service.infrastructure.security.JwtClaimUtils.toStringSet;

@Component
public final class JwtTokenProvider implements TokenProvider {

    private final SecurityJwtProperties props;
    private final Clock clock;
    private final Key key;

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
            claims.put("roles", Set.copyOf(user.roles()));
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
                .signWith(key, SignatureAlgorithm.HS256)
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
            if (!SignatureAlgorithm.HS256.getValue().equals(header.getAlgorithm())) {
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

    // -------------------------
    // Helpers
    // -------------------------
    private static Key resolveKey(SecurityJwtProperties props) {
        if (props.hasSecretBase64()) {
            byte[] decoded = Base64.getDecoder().decode(props.secretBase64());
            if (decoded.length < 32) {
                throw new IllegalStateException("Decoded Base64 JWT secret too short (<32 bytes).");
            }
            return Keys.hmacShaKeyFor(decoded);
        }
        if (props.hasPlainSecret()) {
            byte[] raw = props.secret().getBytes(StandardCharsets.UTF_8);
            if (raw.length < 32) {
                throw new IllegalStateException("Plain JWT secret too short (<32 bytes).");
            }
            return Keys.hmacShaKeyFor(raw);
        }
        throw new IllegalStateException("No JWT secret configured (secretBase64 or secret required).");
    }

}
