package com.renewsim.backend.auth_service.infrastructure.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LoggingEmailAdapter")
class LoggingEmailAdapterTest {

    private LoggingEmailAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LoggingEmailAdapter();
    }

    // ─────────────────────────────────────────────────────
    // sendOtp
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("sendOtp: parámetros válidos → no lanza excepción")
    void sendOtp_validParams_doesNotThrow() {
        assertThatCode(() -> adapter.sendOtp("user@example.com", "123456", 300))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendOtp: email nulo → lanza IllegalArgumentException")
    void sendOtp_nullEmail_throwsIllegalArgument() {
        assertThatThrownBy(() -> adapter.sendOtp(null, "123456", 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toEmail");
    }

    @Test
    @DisplayName("sendOtp: email vacío → lanza IllegalArgumentException")
    void sendOtp_blankEmail_throwsIllegalArgument() {
        assertThatThrownBy(() -> adapter.sendOtp("  ", "123456", 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toEmail");
    }

    @Test
    @DisplayName("sendOtp: OTP nulo → lanza IllegalArgumentException")
    void sendOtp_nullOtp_throwsIllegalArgument() {
        assertThatThrownBy(() -> adapter.sendOtp("user@example.com", null, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rawOtp");
    }

    @Test
    @DisplayName("sendOtp: OTP vacío → lanza IllegalArgumentException")
    void sendOtp_blankOtp_throwsIllegalArgument() {
        assertThatThrownBy(() -> adapter.sendOtp("user@example.com", "", 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rawOtp");
    }

    // ─────────────────────────────────────────────────────
    // sendActivationEmail
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("sendActivationEmail: parámetros válidos → no lanza excepción")
    void sendActivationEmail_validParams_doesNotThrow() {
        assertThatCode(() -> adapter.sendActivationEmail(
                "user@example.com", "some-raw-token-value"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendActivationEmail: email nulo → lanza IllegalArgumentException")
    void sendActivationEmail_nullEmail_throwsIllegalArgument() {
        assertThatThrownBy(() -> adapter.sendActivationEmail(null, "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toEmail");
    }

    @Test
    @DisplayName("sendActivationEmail: email vacío → lanza IllegalArgumentException")
    void sendActivationEmail_blankEmail_throwsIllegalArgument() {
        assertThatThrownBy(() -> adapter.sendActivationEmail("  ", "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toEmail");
    }

    @Test
    @DisplayName("sendActivationEmail: token nulo → lanza IllegalArgumentException")
    void sendActivationEmail_nullToken_throwsIllegalArgument() {
        assertThatThrownBy(() -> adapter.sendActivationEmail("user@example.com", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("activationToken");
    }

    @Test
    @DisplayName("sendActivationEmail: token vacío → lanza IllegalArgumentException")
    void sendActivationEmail_blankToken_throwsIllegalArgument() {
        assertThatThrownBy(() -> adapter.sendActivationEmail("user@example.com", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("activationToken");
    }
}