package com.renewsim.backend.auth_service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ActivationToken domain entity")
class ActivationTokenTest {

    private static final Long USER_ID = 1L;
    private static final String HASH = "hashedtoken123abc";

    // --- issue() factory ---

    @Test
    @DisplayName("issue() creates token with 24-hour TTL, not used, no id")
    void issue_createsValidToken() {
        LocalDateTime before = LocalDateTime.now();

        ActivationToken token = ActivationToken.issue(USER_ID, HASH);

        assertThat(token.getId()).isNull();
        assertThat(token.getUserId()).isEqualTo(USER_ID);
        assertThat(token.getTokenHash()).isEqualTo(HASH);
        assertThat(token.isUsed()).isFalse();
        assertThat(token.getIssuedAt()).isAfterOrEqualTo(before);
        assertThat(token.getExpiresAt()).isAfter(token.getIssuedAt());
        assertThat(token.getExpiresAt())
                .isBeforeOrEqualTo(LocalDateTime.now().plusHours(24).plusSeconds(1));
    }

    @Test
    @DisplayName("issue() throws when userId is null")
    void issue_nullUserId_throws() {
        assertThatThrownBy(() -> ActivationToken.issue(null, HASH))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("issue() throws when tokenHash is blank")
    void issue_blankHash_throws() {
        assertThatThrownBy(() -> ActivationToken.issue(USER_ID, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tokenHash");
    }

    // --- isExpired() ---

    @Test
    @DisplayName("isExpired() returns false for a freshly issued token")
    void isExpired_freshToken_returnsFalse() {
        ActivationToken token = ActivationToken.issue(USER_ID, HASH);
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("isExpired() returns true when expiresAt is in the past")
    void isExpired_pastExpiry_returnsTrue() {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        ActivationToken token = ActivationToken.reconstitute(
                1L, USER_ID, HASH,
                past.minusHours(24), past, false);

        assertThat(token.isExpired()).isTrue();
    }

    // --- isValid() ---

    @Test
    @DisplayName("isValid() returns true when not used and not expired")
    void isValid_notUsedNotExpired_returnsTrue() {
        ActivationToken token = ActivationToken.issue(USER_ID, HASH);
        assertThat(token.isValid()).isTrue();
    }

    @Test
    @DisplayName("isValid() returns false when used")
    void isValid_used_returnsFalse() {
        ActivationToken token = ActivationToken.issue(USER_ID, HASH);
        token.markUsed();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("isValid() returns false when expired")
    void isValid_expired_returnsFalse() {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        ActivationToken token = ActivationToken.reconstitute(
                1L, USER_ID, HASH,
                past.minusHours(24), past, false);

        assertThat(token.isValid()).isFalse();
    }

    // --- markUsed() ---

    @Test
    @DisplayName("markUsed() sets used to true")
    void markUsed_setsUsed() {
        ActivationToken token = ActivationToken.issue(USER_ID, HASH);
        token.markUsed();
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    @DisplayName("markUsed() is idempotent")
    void markUsed_idempotent() {
        ActivationToken token = ActivationToken.issue(USER_ID, HASH);
        token.markUsed();
        token.markUsed();
        assertThat(token.isUsed()).isTrue();
    }

    // --- reconstitute() ---

    @Test
    @DisplayName("reconstitute() throws when expiresAt is not after issuedAt")
    void reconstitute_expiresAtNotAfterIssuedAt_throws() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> ActivationToken.reconstitute(
                1L, USER_ID, HASH, now, now, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiresAt must be after issuedAt");
    }

    @Test
    @DisplayName("reconstitute() restores all fields correctly")
    void reconstitute_restoresFields() {
        LocalDateTime issued = LocalDateTime.now().minusHours(1);
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