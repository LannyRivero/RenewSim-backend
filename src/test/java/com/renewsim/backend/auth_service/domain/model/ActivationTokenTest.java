package com.renewsim.backend.auth_service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ActivationToken domain entity")
class ActivationTokenTest {

    private static final Long USER_ID = 1L;
    private static final String HASH = "hashedtoken123abc";

    // Clock fijo para tests determinísticos
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-24T10:00:00Z"),
            ZoneId.of("UTC"));

    // --- issue() factory ---

    @Test
    @DisplayName("issue() creates token with 24-hour TTL, not used, no id")
    void issue_createsValidToken() {
        LocalDateTime before = LocalDateTime.now(FIXED_CLOCK);

        ActivationToken token = ActivationToken.issue(USER_ID, HASH, FIXED_CLOCK);

        assertThat(token.getId()).isNull();
        assertThat(token.getUserId()).isEqualTo(USER_ID);
        assertThat(token.getTokenHash()).isEqualTo(HASH);
        assertThat(token.isUsed()).isFalse();
        assertThat(token.getIssuedAt()).isAfterOrEqualTo(before);
        assertThat(token.getExpiresAt()).isAfter(token.getIssuedAt());
        assertThat(token.getExpiresAt()).isEqualTo(before.plusHours(24));
    }

    @Test
    @DisplayName("issue() throws when userId is null")
    void issue_nullUserId_throws() {
        assertThatThrownBy(() -> ActivationToken.issue(null, HASH, FIXED_CLOCK))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("issue() throws when tokenHash is blank")
    void issue_blankHash_throws() {
        assertThatThrownBy(() -> ActivationToken.issue(USER_ID, "  ", FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tokenHash");
    }

    // --- isExpired() ---

    @Test
    @DisplayName("isExpired() returns false for a freshly issued token")
    void isExpired_freshToken_returnsFalse() {
        ActivationToken token = ActivationToken.issue(USER_ID, HASH, FIXED_CLOCK);
        assertThat(token.isExpired(FIXED_CLOCK)).isFalse();
    }

    @Test
    @DisplayName("isExpired() returns true when expiresAt is in the past")
    void isExpired_pastExpiry_returnsTrue() {
        // Token emitido en 09:00, expira en 09:00 del día siguiente (24h)
        // Clock actual en 10:00 del día siguiente → expirado
        LocalDateTime issuedAt = LocalDateTime.parse("2026-04-23T09:00:00");
        LocalDateTime expiresAt = LocalDateTime.parse("2026-04-24T09:00:00");

        ActivationToken token = ActivationToken.reconstitute(
                1L, USER_ID, HASH,
                issuedAt, expiresAt, false);

        assertThat(token.isExpired(FIXED_CLOCK)).isTrue();
    }

    // --- isValid() ---

    @Test
    @DisplayName("isValid() returns true when not used and not expired")
    void isValid_notUsedNotExpired_returnsTrue() {
        ActivationToken token = ActivationToken.issue(USER_ID, HASH, FIXED_CLOCK);
        assertThat(token.isValid(FIXED_CLOCK)).isTrue();
    }

    @Test
    @DisplayName("isValid() returns false when used")
    void isValid_used_returnsFalse() {
        ActivationToken token = ActivationToken.issue(USER_ID, HASH, FIXED_CLOCK);
        token.markUsed();
        assertThat(token.isValid(FIXED_CLOCK)).isFalse();
    }

    @Test
    @DisplayName("isValid() returns false when expired")
    void isValid_expired_returnsFalse() {
        // Token emitido en 09:00, expira en 09:00 del día siguiente
        // Clock actual en 10:00 → expirado
        LocalDateTime issuedAt = LocalDateTime.parse("2026-04-23T09:00:00");
        LocalDateTime expiresAt = LocalDateTime.parse("2026-04-24T09:00:00");

        ActivationToken token = ActivationToken.reconstitute(
                1L, USER_ID, HASH,
                issuedAt, expiresAt, false);

        assertThat(token.isValid(FIXED_CLOCK)).isFalse();
    }

    // --- markUsed() ---

    @Test
    @DisplayName("markUsed() sets used to true")
    void markUsed_setsUsed() {
        ActivationToken token = ActivationToken.issue(USER_ID, HASH, FIXED_CLOCK);
        token.markUsed();
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    @DisplayName("markUsed() is idempotent")
    void markUsed_idempotent() {
        ActivationToken token = ActivationToken.issue(USER_ID, HASH, FIXED_CLOCK);
        token.markUsed();
        token.markUsed();
        assertThat(token.isUsed()).isTrue();
    }

    // --- reconstitute() ---

    @Test
    @DisplayName("reconstitute() throws when expiresAt is not after issuedAt")
    void reconstitute_expiresAtNotAfterIssuedAt_throws() {
        LocalDateTime now = LocalDateTime.now(FIXED_CLOCK);
        assertThatThrownBy(() -> ActivationToken.reconstitute(
                1L, USER_ID, HASH, now, now, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiresAt must be after issuedAt");
    }

    @Test
    @DisplayName("reconstitute() restores all fields correctly")
    void reconstitute_restoresFields() {
        LocalDateTime issued = LocalDateTime.parse("2026-04-23T10:00:00");
        LocalDateTime expires = issued.plusHours(24);

        ActivationToken token = ActivationToken.reconstitute(
                42L, USER_ID, HASH, issued, expires, true);

        assertThat(token.getId()).isEqualTo(42L);
        assertThat(token.getUserId()).isEqualTo(USER_ID);
        assertThat(token.getTokenHash()).isEqualTo(HASH);
        assertThat(token.getIssuedAt()).isEqualTo(issued);
        assertThat(token.getExpiresAt()).isEqualTo(expires);
        assertThat(token.isUsed()).isTrue();
    }
}