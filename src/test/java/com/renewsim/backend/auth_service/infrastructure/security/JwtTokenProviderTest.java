package com.renewsim.backend.auth_service.infrastructure.security;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.config.SecurityJwtProperties;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static String randomBase64Key() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    private static SecurityJwtProperties props(
            String issuer,
            String audience,
            String secretBase64,
            Long expirationSeconds,
            Long nbfSkewSeconds,
            Long clockSkewSeconds,
            Long serviceExpirationSeconds) {
        // Ajusta a tu record real: issuer, audience, secret, secretBase64,
        // expirationSeconds, notBeforeSkewSeconds, allowedClockSkewSeconds
        return new SecurityJwtProperties(
                issuer,
                audience,
                null,
                secretBase64,
                expirationSeconds,
                nbfSkewSeconds,
                clockSkewSeconds,
                serviceExpirationSeconds

        );
    }

    private static JwtParser parserWith(String base64Key, Clock clock, String reqIss, String reqAud, Long skew) {
        JwtParserBuilder b = Jwts.parserBuilder()
                .requireIssuer(reqIss)
                .requireAudience(reqAud)
                .setSigningKey(Base64.getDecoder().decode(base64Key))
                .setClock(() -> Date.from(Instant.now(clock)));
        if (skew != null && skew > 0)
            b.setAllowedClockSkewSeconds(skew);
        return b.build();
    }

    @Test
    @DisplayName("generate/validate → token válido con roles/scopes y claims estándar")
    void generateAndValidate_ok_withStandardClaims() {
        String base64Key = randomBase64Key();
        Instant base = Instant.parse("2025-01-01T10:00:00Z");
        Clock clock = Clock.fixed(base, ZoneOffset.UTC);

        var p = props("renewsim-auth", "renewsim-app", base64Key, 3600L, 0L, 60L, 3600L);
        var provider = new JwtTokenProvider(p, clock);

        var user = new AuthenticatedUser("john", Set.of("USER"), Set.of("read"));
        String token = provider.generate(user);

        var res = provider.validate(token);
        assertThat(res).isPresent();
        assertThat(res.get().username()).isEqualTo("john");
        assertThat(res.get().roles()).containsExactly("ROLE_USER");
        assertThat(res.get().scopes()).containsExactly("read");

        Claims claims = parserWith(base64Key, clock, "renewsim-auth", "renewsim-app", 60L)
                .parseClaimsJws(token).getBody();
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.getIssuer()).isEqualTo("renewsim-auth");
        assertThat(claims.getAudience()).isEqualTo("renewsim-app");
        assertThat(claims.getSubject()).isEqualTo("john");
        assertThat(claims.getIssuedAt()).isEqualTo(Date.from(base));
        assertThat(claims.getNotBefore()).isEqualTo(Date.from(base));
        assertThat(claims.getExpiration()).isEqualTo(Date.from(base.plusSeconds(3600)));
    }

    @Test
    @DisplayName("validate → vacío cuando issuer es incorrecto")
    void validate_empty_wrongIssuer() {
        String base64Key = randomBase64Key();
        Instant base = Instant.parse("2025-01-01T10:00:00Z");
        Clock clock = Clock.fixed(base, ZoneOffset.UTC);

        var goodProps = props("renewsim-auth", "renewsim-app", base64Key, 3600L, 0L, 60L,3600L);
        var signer = new JwtTokenProvider(goodProps, clock);
        String token = signer.generate(new AuthenticatedUser("john", Set.of(), Set.of()));

        var badProps = props("WRONG", "renewsim-app", base64Key, 3600L, 0L, 60L,3600L);
        var validator = new JwtTokenProvider(badProps, clock);

        assertThat(validator.validate(token)).isEmpty();
    }

    @Test
    @DisplayName("validate → vacío cuando audience es incorrecta")
    void validate_empty_wrongAudience() {
        String base64Key = randomBase64Key();
        Instant base = Instant.parse("2025-01-01T10:00:00Z");
        Clock clock = Clock.fixed(base, ZoneOffset.UTC);

        var goodProps = props("renewsim-auth", "renewsim-app", base64Key, 3600L, 0L, 60L,3600L);
        var signer = new JwtTokenProvider(goodProps, clock);
        String token = signer.generate(new AuthenticatedUser("john", Set.of(), Set.of()));

        var badProps = props("renewsim-auth", "WRONG", base64Key, 3600L, 0L, 60L,3600L);
        var validator = new JwtTokenProvider(badProps, clock);

        assertThat(validator.validate(token)).isEmpty();
    }

    @Test
    @DisplayName("validate → vacío si nbf está en el futuro (Premature)")
    void validate_empty_beforeNbf() {
        String base64Key = randomBase64Key();
        Instant base = Instant.parse("2025-01-01T10:00:00Z");
        Clock clock = Clock.fixed(base, ZoneOffset.UTC);

        var p = props("renewsim-auth", "renewsim-app", base64Key, 3600L, 60L, 0L, 3600L);
        var provider = new JwtTokenProvider(p, clock);

        String token = provider.generate(new AuthenticatedUser("john", Set.of(), Set.of()));

        assertThat(provider.validate(token)).isEmpty();
    }

    @Test
    @DisplayName("validate → ok dentro del clock skew luego de expirar")
    void validate_ok_withinAllowedSkew() {
        String base64Key = randomBase64Key();
        Instant base = Instant.parse("2025-01-01T10:00:00Z");

        long expSeconds = 30;
        long skewSeconds = 20;

        var p = props("renewsim-auth", "renewsim-app", base64Key, expSeconds, 0L, skewSeconds, 3600L);

        JwtTokenProvider signer = new JwtTokenProvider(p, Clock.fixed(base, ZoneOffset.UTC));
        String token = signer.generate(new AuthenticatedUser("john", Set.of("USER"), Set.of("read")));

        JwtTokenProvider validatorWithinSkew = new JwtTokenProvider(p,
                Clock.fixed(base.plusSeconds(45), ZoneOffset.UTC));
        assertThat(validatorWithinSkew.validate(token)).isPresent();
    }

    @Test
    @DisplayName("validate → vacío cuando expira más allá del clock skew")
    void validate_empty_outsideSkew() {
        String base64Key = randomBase64Key();
        Instant base = Instant.parse("2025-01-01T10:00:00Z");

        long expSeconds = 30;
        long skewSeconds = 20;

        var p = props("renewsim-auth", "renewsim-app", base64Key, expSeconds, 0L, skewSeconds, 3600L);

        JwtTokenProvider signer = new JwtTokenProvider(p, Clock.fixed(base, ZoneOffset.UTC));
        String token = signer.generate(new AuthenticatedUser("john", Set.of("USER"), Set.of("read")));

        JwtTokenProvider validatorOutsideSkew = new JwtTokenProvider(p,
                Clock.fixed(base.plusSeconds(60), ZoneOffset.UTC));
        assertThat(validatorOutsideSkew.validate(token)).isEmpty();
    }

    @Test
    @DisplayName("validate → vacío para token firmado con otra clave (firma inválida)")
    void validate_empty_differentKey() {
        String base64Key = randomBase64Key();
        String attackerKey = randomBase64Key();
        Instant base = Instant.parse("2025-01-01T10:00:00Z");
        Clock clock = Clock.fixed(base, ZoneOffset.UTC);

        var p = props("renewsim-auth", "renewsim-app", base64Key, 60L, 0L, 0L,60L);
        JwtTokenProvider validator = new JwtTokenProvider(p, clock);

        Key attackerSigningKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(attackerKey));
        String forged = Jwts.builder()
                .setIssuer("renewsim-auth")
                .setAudience("renewsim-app")
                .setSubject("john")
                .setIssuedAt(Date.from(base))
                .setNotBefore(Date.from(base))
                .setExpiration(Date.from(base.plusSeconds(60)))
                .signWith(attackerSigningKey, SignatureAlgorithm.HS256)
                .compact();

        assertThat(validator.validate(forged)).isEmpty();
    }

    @Test
    @DisplayName("validate → vacío para token con algoritmo distinto (HS384)")
    void validate_empty_differentAlg() {
        String base64Key = randomBase64Key();
        Instant base = Instant.parse("2025-01-01T10:00:00Z");
        Clock clock = Clock.fixed(base, ZoneOffset.UTC);

        var p = props("renewsim-auth", "renewsim-app", base64Key, 60L, 0L, 0L, 60L);
        JwtTokenProvider validator = new JwtTokenProvider(p, clock);

        Key hs384Key = Keys.secretKeyFor(SignatureAlgorithm.HS384);
        String hs384Token = Jwts.builder()
                .setIssuer("renewsim-auth")
                .setAudience("renewsim-app")
                .setSubject("john")
                .setIssuedAt(Date.from(base))
                .setNotBefore(Date.from(base))
                .setExpiration(Date.from(base.plusSeconds(60)))
                .signWith(hs384Key, SignatureAlgorithm.HS384)
                .compact();

        assertThat(validator.validate(hs384Token)).isEmpty();
    }

    @Test
    @DisplayName("constructor → lanza excepción si la clave < 32 bytes (secret plano)")
    void constructor_throws_shortPlainSecret() {
        var tooShortPlain = "short-key";
        var p = new SecurityJwtProperties(
                "iss", "aud",
                tooShortPlain,
                null,
                3600L,
                0L,
                0L,
                3600L);
        Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);

        assertThatThrownBy(() -> new JwtTokenProvider(p, clock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Plain JWT secret too short");
    }

    @Test
    @DisplayName("expiresInSeconds → devuelve el valor configurado")
    void expiresInSeconds_ok() {
        String base64Key = randomBase64Key();
        Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);

        var p = props("renewsim-auth", "renewsim-app", base64Key, 1234L, 0L, 0L, 3600L);
        JwtTokenProvider provider = new JwtTokenProvider(p, clock);

        assertThat(provider.expiresInSeconds()).isEqualTo(1234L);
    }

    @Test
    @DisplayName("validate → ok para token sin roles ni scopes")
    void validate_ok_subjectOnly() {
        String base64Key = randomBase64Key();
        Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);

        var p = props("renewsim-auth", "renewsim-app", base64Key, 3600L, 0L, 60L, 3600L);
        JwtTokenProvider provider = new JwtTokenProvider(p, clock);

        String token = provider.generate(new AuthenticatedUser("only-subject", null, null));
        var result = provider.validate(token);

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("only-subject");
        assertThat(result.get().roles()).isEmpty();
        assertThat(result.get().scopes()).isEmpty();
    }

    @Test
    @DisplayName("toStringSet → vacío cuando el claim no es colección")
    void toStringSet_ShouldReturnEmpty_WhenClaimNotCollection() throws Exception {
        Method m = JwtTokenProvider.class.getDeclaredMethod("toStringSet", Object.class);
        m.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) m.invoke(null, "single-role");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("generate → incluye jti único")
    void generate_includesJti() {
        String base64Key = randomBase64Key();
        Instant base = Instant.parse("2025-01-01T10:00:00Z");
        Clock clock = Clock.fixed(base, ZoneOffset.UTC);

        var p = props("renewsim-auth", "renewsim-app", base64Key, 3600L, 0L, 60L, 3600L);
        var provider = new JwtTokenProvider(p, clock);

        String token = provider.generate(new AuthenticatedUser("john", Set.of(), Set.of()));

        Claims claims = parserWith(base64Key, clock, "renewsim-auth", "renewsim-app", 60L)
                .parseClaimsJws(token).getBody();

        assertThat(claims.getId()).isNotBlank();
        assertThat(assertUUID(claims.getId())).isTrue();
    }

    @Test
    @DisplayName("constructor → lanza excepción cuando faltan ambos secretos (plain y base64)")
    void constructor_throws_whenBothSecretsMissing() {
        var p = new SecurityJwtProperties(
                "iss", "aud",
                null,
                null,
                3600L,
                0L,
                0L,
                3600L);
        Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);

        assertThatThrownBy(() -> new JwtTokenProvider(p, clock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No JWT secret configured");
    }

    @Test
    @DisplayName("constructor → NO debe ser público (package-private)")
    void constructor_isPackagePrivate() throws Exception {
        var ctor = JwtTokenProvider.class.getDeclaredConstructor(SecurityJwtProperties.class, Clock.class);
        int mod = ctor.getModifiers();
        assertThat(java.lang.reflect.Modifier.isPublic(mod)).isFalse();
        assertThat(java.lang.reflect.Modifier.isPrivate(mod)).isFalse();
        assertThat(java.lang.reflect.Modifier.isProtected(mod)).isFalse();
    }

    private static boolean assertUUID(String maybeUuid) {
        try {
            UUID.fromString(maybeUuid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
