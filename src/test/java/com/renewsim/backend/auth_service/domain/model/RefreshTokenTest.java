package com.renewsim.backend.auth_service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RefreshToken domain entity")
class RefreshTokenTest {

    private static final Long USER_ID = 1L;
    private static final String HASH = "hashedrefreshtoken456xyz";

    // --- issue() factory ---

    @Test
    @DisplayName("issue() creates token with 7-day TTL, not revoked, no id")
    void issue_createsValidToken() {
        LocalDateTime before = LocalDateTime.now();

        RefreshToken token = RefreshToken.issue(USER_ID, HASH);

        assertThat(token.getId()).isNull();
        assertThat(token.getUserId()).isEqualTo(USER_ID);
        assertThat(token.getTokenHash()).isEqualTo(HASH);
        assertThat(token.isRevoked()).isFalse();
        assertThat(token.getIssuedAt()).isAfterOrEqualTo(before);
        assertThat(token.getExpiresAt()).isAfter(token.getIssuedAt());
        assertThat(token.getExpiresAt())
                .isBeforeOrEqualTo(LocalDateTime.now().plusDays(7).plusSeconds(1));
    }

    @Test
    @DisplayName("issue() throws when userId is null")
    void issue_nullUserId_throws() {
        assertThatThrownBy(() -> RefreshToken.issue(null, HASH))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("issue() throws when tokenHash is blank")
    void issue_blankHash_throws() {
        assertThatThrownBy(() -> RefreshToken.issue(USER_ID, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tokenHash");
    }

    // --- isValid() ---

    @Test
    @DisplayName("isValid() returns true when not revoked and not expired")
    void isValid_notRevokedNotExpired_returnsTrue() {
        RefreshToken token = RefreshToken.issue(USER_ID, HASH);
        assertThat(token.isValid()).isTrue();
    }

    @Test
    @DisplayName("isValid() returns false when revoked")
    void isValid_revoked_returnsFalse() {
        RefreshToken token = RefreshToken.issue(USER_ID, HASH);
        token.revoke();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("isValid() returns false when expired")
    void isValid_expired_returnsFalse() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        RefreshToken token = RefreshToken.reconstitute(
                1L, USER_ID, HASH,
                past.minusDays(7), past, false);

        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("isValid() returns false when both revoked and expired")
    void isValid_revokedAndExpired_returnsFalse() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        RefreshToken token = RefreshToken.reconstitute(
                1L, USER_ID, HASH,
                past.minusDays(7), past, true);

        assertThat(token.isValid()).isFalse();
    }

    // --- revoke() ---

    @Test
    @DisplayName("revoke() sets revoked to true")
    void revoke_setsRevoked() {
        RefreshToken token = RefreshToken.issue(USER_ID, HASH);
        token.revoke();
        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("revoke() is idempotent")
    void revoke_idempotent() {
        RefreshToken token = RefreshToken.issue(USER_ID, HASH);
        token.revoke();
        token.revoke();
        assertThat(token.isRevoked()).isTrue();
    }

    // --- reconstitute() ---

    @Test
    @DisplayName("reconstitute() throws when expiresAt is not after issuedAt")
    void reconstitute_expiresAtNotAfterIssuedAt_throws() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> RefreshToken.reconstitute(
                1L, USER_ID, HASH, now, now, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiresAt must be after issuedAt");
    }

    @Test
    @DisplayName("reconstitute() restores all fields correctly")
    void reconstitute_restoresFields() {
        LocalDateTime issued = LocalDateTime.now().minusDays(1);
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