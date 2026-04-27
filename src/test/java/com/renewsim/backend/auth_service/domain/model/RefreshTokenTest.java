package com.renewsim.backend.auth_service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RefreshToken domain entity")
class RefreshTokenTest {

    private static final Long USER_ID = 1L;
    private static final String HASH = "hashedrefreshtoken456xyz";
    
    // Clock fijo para tests determinísticos
    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-04-24T10:00:00Z"),
        ZoneId.of("UTC")
    );

    // --- issue() factory ---

    @Test
    @DisplayName("issue() creates token with 7-day TTL, not revoked, no id")
    void issue_createsValidToken() {
        LocalDateTime before = LocalDateTime.now(FIXED_CLOCK);

        RefreshToken token = RefreshToken.issue(USER_ID, HASH, FIXED_CLOCK);

        assertThat(token.getId()).isNull();
        assertThat(token.getUserId()).isEqualTo(USER_ID);
        assertThat(token.getTokenHash()).isEqualTo(HASH);
        assertThat(token.isRevoked()).isFalse();
        assertThat(token.getIssuedAt()).isAfterOrEqualTo(before);
        assertThat(token.getExpiresAt()).isAfter(token.getIssuedAt());
        assertThat(token.getExpiresAt()).isEqualTo(before.plusDays(7));
    }

    @Test
    @DisplayName("issue() throws when userId is null")
    void issue_nullUserId_throws() {
        assertThatThrownBy(() -> RefreshToken.issue(null, HASH, FIXED_CLOCK))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("issue() throws when tokenHash is blank")
    void issue_blankHash_throws() {
        assertThatThrownBy(() -> RefreshToken.issue(USER_ID, "  ", FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tokenHash");
    }

    // --- isValid() ---

    @Test
    @DisplayName("isValid() returns true when not revoked and not expired")
    void isValid_notRevokedNotExpired_returnsTrue() {
        RefreshToken token = RefreshToken.issue(USER_ID, HASH, FIXED_CLOCK);
        assertThat(token.isValid(FIXED_CLOCK)).isTrue();
    }

    @Test
    @DisplayName("isValid() returns false when revoked")
    void isValid_revoked_returnsFalse() {
        RefreshToken token = RefreshToken.issue(USER_ID, HASH, FIXED_CLOCK);
        RefreshToken revoked = token.revoked();
        assertThat(revoked.isValid(FIXED_CLOCK)).isFalse();
    }

    @Test
    @DisplayName("isValid() returns false when expired")
    void isValid_expired_returnsFalse() {
        // Token emitido el 17/04, expira el 24/04 a las 09:00
        // Clock actual 24/04 a las 10:00 → expirado
        LocalDateTime issuedAt = LocalDateTime.parse("2026-04-17T09:00:00");
        LocalDateTime expiresAt = LocalDateTime.parse("2026-04-24T09:00:00");
        
        RefreshToken token = RefreshToken.reconstitute(
                1L, USER_ID, HASH,
                issuedAt, expiresAt, false);

        assertThat(token.isValid(FIXED_CLOCK)).isFalse();
    }

    @Test
    @DisplayName("isValid() returns false when both revoked and expired")
    void isValid_revokedAndExpired_returnsFalse() {
        // Token emitido el 17/04, expira el 24/04 a las 09:00
        // Clock actual 24/04 a las 10:00 → expirado Y revocado
        LocalDateTime issuedAt = LocalDateTime.parse("2026-04-17T09:00:00");
        LocalDateTime expiresAt = LocalDateTime.parse("2026-04-24T09:00:00");
        
        RefreshToken token = RefreshToken.reconstitute(
                1L, USER_ID, HASH,
                issuedAt, expiresAt, true);

        assertThat(token.isValid(FIXED_CLOCK)).isFalse();
    }

    // --- revoked() ---

    @Test
    @DisplayName("revoked() creates new token with revoked=true")
    void revoked_createsNewToken() {
        RefreshToken token = RefreshToken.issue(USER_ID, HASH, FIXED_CLOCK);
        RefreshToken revoked = token.revoked();
        assertThat(revoked.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("revoked() preserves original token (immutability)")
    void revoked_originalTokenUnchanged() {
        RefreshToken token = RefreshToken.issue(USER_ID, HASH, FIXED_CLOCK);
        RefreshToken revoked = token.revoked();
        assertThat(token.isRevoked()).isFalse();
        assertThat(token.isValid(FIXED_CLOCK)).isTrue();
    }

    @Test
    @DisplayName("revoked() is idempotent")
    void revoked_idempotent() {
        RefreshToken token = RefreshToken.issue(USER_ID, HASH, FIXED_CLOCK);
        RefreshToken revoked1 = token.revoked();
        RefreshToken revoked2 = token.revoked();
        assertThat(revoked1.isRevoked()).isTrue();
        assertThat(revoked2.isRevoked()).isTrue();
    }

    // --- reconstitute() ---

    @Test
    @DisplayName("reconstitute() throws when expiresAt is not after issuedAt")
    void reconstitute_expiresAtNotAfterIssuedAt_throws() {
        LocalDateTime now = LocalDateTime.now(FIXED_CLOCK);
        assertThatThrownBy(() -> RefreshToken.reconstitute(
                1L, USER_ID, HASH, now, now, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiresAt must be after issuedAt");
    }

    @Test
    @DisplayName("reconstitute() restores all fields correctly")
    void reconstitute_restoresFields() {
        LocalDateTime issued = LocalDateTime.parse("2026-04-23T10:00:00");
        LocalDateTime expires = issued.plusDays(7);

        RefreshToken token = RefreshToken.reconstitute(
                99L, USER_ID, HASH, issued, expires, true);

        assertThat(token.getId()).isEqualTo(99L);
        assertThat(token.getUserId()).isEqualTo(USER_ID);
        assertThat(token.getTokenHash()).isEqualTo(HASH);
        assertThat(token.getIssuedAt()).isEqualTo(issued);
        assertThat(token.getExpiresAt()).isEqualTo(expires);
        assertThat(token.isRevoked()).isTrue();
    }
}