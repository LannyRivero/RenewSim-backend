package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginStep2Command;
import com.renewsim.backend.auth_service.application.port.out.OtpCodeRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LoginStep2ResultDTO;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.model.RefreshToken;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginStep2Service")
class LoginStep2ServiceTest {

    @Mock private UserAccountGateway userAccountGateway;
    @Mock private OtpCodeRepositoryPort otpCodeRepositoryPort;
    @Mock private RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    @Mock private TokenProvider tokenProvider;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginStep2Service service;

    private UserSnapshot activeUser;
    private OtpCode validOtp;

    @BeforeEach
    void setUp() {
        activeUser = new UserSnapshot(
                1L, "john", "hashedpass", "john@example.com",
                Set.of(RoleName.USER), true);

        validOtp = OtpCode.issue(1L, "$hashed_otp", OtpCode.Purpose.LOGIN);
    }

    @Test
    @DisplayName("valid OTP -> returns access token and persists refresh token")
    void execute_validOtp_returnsTokens() {
        when(userAccountGateway.findByEmail("john@example.com"))
                .thenReturn(Optional.of(activeUser));
        when(otpCodeRepositoryPort.findLatestValidByUserId(1L, OtpCode.Purpose.LOGIN))
                .thenReturn(Optional.of(validOtp));
        when(passwordEncoder.matches("123456", "$hashed_otp")).thenReturn(true);
        when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn("jwt-token");
        when(tokenProvider.expiresInSeconds()).thenReturn(3600L);
        when(passwordEncoder.encode(anyString())).thenReturn("$hashed_refresh");
        when(refreshTokenRepositoryPort.save(any(RefreshToken.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(otpCodeRepositoryPort.save(any(OtpCode.class)))
                .thenAnswer(i -> i.getArgument(0));

        LoginStep2ResultDTO result = service.execute(
                new LoginStep2Command("john@example.com", "123456"));

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(3600L);
        assertThat(result.username()).isEqualTo("john@example.com");
        assertThat(result.roles()).contains("USER");

        verify(otpCodeRepositoryPort).save(any(OtpCode.class));
        verify(refreshTokenRepositoryPort).save(any(RefreshToken.class));
        assertThat(validOtp.isUsed()).isTrue();
    }

    @Test
    @DisplayName("unknown email -> throws AuthenticationException")
    void execute_unknownEmail_throws() {
        when(userAccountGateway.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new LoginStep2Command("unknown@example.com", "123456")))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("disabled user -> throws AuthenticationException")
    void execute_disabledUser_throws() {
        UserSnapshot disabled = new UserSnapshot(
                2L, "jane", "hashedpass", "jane@example.com",
                Set.of(RoleName.USER), false);

        when(userAccountGateway.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(disabled));

        assertThatThrownBy(() -> service.execute(
                new LoginStep2Command("jane@example.com", "123456")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("no valid OTP found -> throws AuthenticationException")
    void execute_noValidOtp_throws() {
        when(userAccountGateway.findByEmail("john@example.com"))
                .thenReturn(Optional.of(activeUser));
        when(otpCodeRepositoryPort.findLatestValidByUserId(1L, OtpCode.Purpose.LOGIN))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new LoginStep2Command("john@example.com", "123456")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("OTP");
    }

    @Test
    @DisplayName("wrong OTP code -> throws AuthenticationException")
    void execute_wrongOtpCode_throws() {
        when(userAccountGateway.findByEmail("john@example.com"))
                .thenReturn(Optional.of(activeUser));
        when(otpCodeRepositoryPort.findLatestValidByUserId(1L, OtpCode.Purpose.LOGIN))
                .thenReturn(Optional.of(validOtp));
        when(passwordEncoder.matches("wrongotp", "$hashed_otp")).thenReturn(false);

        assertThatThrownBy(() -> service.execute(
                new LoginStep2Command("john@example.com", "wrongotp")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("OTP");

        verify(otpCodeRepositoryPort, never()).save(any());
        verify(refreshTokenRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("valid OTP -> OTP is marked as used after successful login")
    void execute_validOtp_otpMarkedUsed() {
        when(userAccountGateway.findByEmail("john@example.com"))
                .thenReturn(Optional.of(activeUser));
        when(otpCodeRepositoryPort.findLatestValidByUserId(1L, OtpCode.Purpose.LOGIN))
                .thenReturn(Optional.of(validOtp));
        when(passwordEncoder.matches("123456", "$hashed_otp")).thenReturn(true);
        when(tokenProvider.generate(any())).thenReturn("jwt");
        when(tokenProvider.expiresInSeconds()).thenReturn(3600L);
        when(passwordEncoder.encode(anyString())).thenReturn("$hashed_refresh");
        when(refreshTokenRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));
        when(otpCodeRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

        service.execute(new LoginStep2Command("john@example.com", "123456"));

        assertThat(validOtp.isUsed()).isTrue();
    }
}