package com.renewsim.backend.auth_service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OtpCode domain entity")
class OtpCodeTest {

    private static final Long USER_ID = 1L;
    private static final String HASH = "$2a$12$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";

    // --- issue() factory ---

    @Test
    @DisplayName("issue() creates OTP with 5-minute TTL, not used, no id")
    void issue_createsValidOtp() {
        LocalDateTime before = LocalDateTime.now();

        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN);

        assertThat(otp.getId()).isNull();
        assertThat(otp.getUserId()).isEqualTo(USER_ID);
        assertThat(otp.getCodeHash()).isEqualTo(HASH);
        assertThat(otp.getPurpose()).isEqualTo(OtpCode.Purpose.LOGIN);
        assertThat(otp.isUsed()).isFalse();
        assertThat(otp.getIssuedAt()).isAfterOrEqualTo(before);
        assertThat(otp.getExpiresAt()).isAfter(otp.getIssuedAt());
        assertThat(otp.getExpiresAt())
                .isBeforeOrEqualTo(LocalDateTime.now().plusMinutes(5).plusSeconds(1));
    }

    @Test
    @DisplayName("issue() throws when userId is null")
    void issue_nullUserId_throws() {
        assertThatThrownBy(() -> OtpCode.issue(null, HASH, OtpCode.Purpose.LOGIN))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("issue() throws when codeHash is blank")
    void issue_blankHash_throws() {
        assertThatThrownBy(() -> OtpCode.issue(USER_ID, "  ", OtpCode.Purpose.LOGIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("codeHash");
    }

    @Test
    @DisplayName("issue() throws when purpose is null")
    void issue_nullPurpose_throws() {
        assertThatThrownBy(() -> OtpCode.issue(USER_ID, HASH, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("purpose");
    }

    // --- isExpired() ---

    @Test
    @DisplayName("isExpired() returns false for a freshly issued OTP")
    void isExpired_freshOtp_returnsFalse() {
        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN);
        assertThat(otp.isExpired()).isFalse();
    }

    @Test
    @DisplayName("isExpired() returns true when expiresAt is in the past")
    void isExpired_pastExpiry_returnsTrue() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(1);
        OtpCode otp = OtpCode.reconstitute(
                1L, USER_ID, HASH, OtpCode.Purpose.LOGIN,
                past.minusMinutes(5), past, false);

        assertThat(otp.isExpired()).isTrue();
    }

    // --- isValid() ---

    @Test
    @DisplayName("isValid() returns true when not used and not expired")
    void isValid_notUsedNotExpired_returnsTrue() {
        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN);
        assertThat(otp.isValid()).isTrue();
    }

    @Test
    @DisplayName("isValid() returns false when used")
    void isValid_used_returnsFalse() {
        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN);
        otp.markUsed();
        assertThat(otp.isValid()).isFalse();
    }

    @Test
    @DisplayName("isValid() returns false when expired")
    void isValid_expired_returnsFalse() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(1);
        OtpCode otp = OtpCode.reconstitute(
                1L, USER_ID, HASH, OtpCode.Purpose.LOGIN,
                past.minusMinutes(5), past, false);

        assertThat(otp.isValid()).isFalse();
    }

    // --- markUsed() ---

    @Test
    @DisplayName("markUsed() sets used to true")
    void markUsed_setsUsed() {
        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN);
        otp.markUsed();
        assertThat(otp.isUsed()).isTrue();
    }

    @Test
    @DisplayName("markUsed() is idempotent")
    void markUsed_idempotent() {
        OtpCode otp = OtpCode.issue(USER_ID, HASH, OtpCode.Purpose.LOGIN);
        otp.markUsed();
        otp.markUsed();
        assertThat(otp.isUsed()).isTrue();
    }

    // --- reconstitute() ---

    @Test
    @DisplayName("reconstitute() throws when expiresAt is not after issuedAt")
    void reconstitute_expiresAtNotAfterIssuedAt_throws() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> OtpCode.reconstitute(
                1L, USER_ID, HASH, OtpCode.Purpose.LOGIN,
                now, now, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiresAt must be after issuedAt");
    }
}