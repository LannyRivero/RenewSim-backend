package com.renewsim.backend.auth_service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OtpCode domain entity")
class OtpCodeTest {

    private static final Long USER_ID = 1L;
    private static final String HASH = "$2a$12$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-24T10:00:00Z"),
            ZoneId.of("UTC"));

    // --- issue() factory ---

    @Test
    @DisplayName("issue() creates OTP with 5-minute TTL, not used, no id")
    void issue_createsValidOtp() {
        LocalDateTime before = LocalDateTime.now(FIXED_CLOCK);

        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN, FIXED_CLOCK);

        assertThat(otp.getId()).isNull();
        assertThat(otp.getUserId()).isEqualTo(USER_ID);
        assertThat(otp.getCodeHash()).isEqualTo(HASH);
        assertThat(otp.getPurpose()).isEqualTo(OtpCode.Purpose.LOGIN);
        assertThat(otp.isUsed()).isFalse();
        assertThat(otp.getIssuedAt()).isAfterOrEqualTo(before);
        assertThat(otp.getExpiresAt()).isAfter(otp.getIssuedAt());
        assertThat(otp.getExpiresAt())
                .isEqualTo(before.plusMinutes(5));
    }

    @Test
    @DisplayName("issue() throws when userId is null")
    void issue_nullUserId_throws() {
        assertThatThrownBy(() -> OtpCode.issue(null, HASH, OtpCode.Purpose.LOGIN, FIXED_CLOCK))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("issue() throws when codeHash is blank")
    void issue_blankHash_throws() {
        assertThatThrownBy(() -> OtpCode.issue(USER_ID, "  ", OtpCode.Purpose.LOGIN, FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("codeHash");
    }

    @Test
    @DisplayName("issue() throws when purpose is null")
    void issue_nullPurpose_throws() {
        assertThatThrownBy(() -> OtpCode.issue(USER_ID, HASH, null, FIXED_CLOCK))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("purpose");
    }

    @Test
    @DisplayName("isExpired() returns false for a freshly issued OTP")
    void isExpired_freshOtp_returnsFalse() {
        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN, FIXED_CLOCK);
        assertThat(otp.isExpired(FIXED_CLOCK)).isFalse();
    }

    @Test
    @DisplayName("isExpired() returns true when expiresAt is in the past")
    void isExpired_pastExpiry_returnsTrue() {
        LocalDateTime issuedAt = LocalDateTime.parse("2026-04-24T09:54:00");
        LocalDateTime expiresAt = LocalDateTime.parse("2026-04-24T09:59:00");

        OtpCode otp = OtpCode.reconstitute(
                1L, USER_ID, HASH, OtpCode.Purpose.LOGIN,
                issuedAt, expiresAt, false);

        assertThat(otp.isExpired(FIXED_CLOCK)).isTrue();
    }

    @Test
    @DisplayName("isValid() returns true when not used and not expired")
    void isValid_notUsedNotExpired_returnsTrue() {
        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN, FIXED_CLOCK);
        assertThat(otp.isValid(FIXED_CLOCK)).isTrue();
    }

    @Test
    @DisplayName("isValid() returns false when used")
    void isValid_used_returnsFalse() {
        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN, FIXED_CLOCK);
        OtpCode used = otp.markUsed();
        assertThat(used.isValid(FIXED_CLOCK)).isFalse();
    }

    @Test
    @DisplayName("isValid() returns false when expired")
    void isValid_expired_returnsFalse() {
        LocalDateTime issuedAt = LocalDateTime.parse("2026-04-24T09:54:00");
        LocalDateTime expiresAt = LocalDateTime.parse("2026-04-24T09:59:00");

        OtpCode otp = OtpCode.reconstitute(
                1L, USER_ID, HASH, OtpCode.Purpose.LOGIN,
                issuedAt, expiresAt, false);

        assertThat(otp.isValid(FIXED_CLOCK)).isFalse();
    }

    @Test
    @DisplayName("markUsed() sets used to true")
    void markUsed_setsUsed() {
        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN, FIXED_CLOCK);
        OtpCode used = otp.markUsed();
        assertThat(used.isUsed()).isTrue();
    }

    @Test
    @DisplayName("markUsed() is idempotent")
    void markUsed_idempotent() {
        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN, FIXED_CLOCK);
        OtpCode used = otp.markUsed().markUsed();
        assertThat(used.isUsed()).isTrue();
    }

    @Test
    @DisplayName("reconstitute() throws when expiresAt is not after issuedAt")
    void reconstitute_expiresAtNotAfterIssuedAt_throws() {
        LocalDateTime now = LocalDateTime.now(FIXED_CLOCK);
        assertThatThrownBy(() -> OtpCode.reconstitute(
                1L, USER_ID, HASH, OtpCode.Purpose.LOGIN,
                now, now, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiresAt must be after issuedAt");
    }
}