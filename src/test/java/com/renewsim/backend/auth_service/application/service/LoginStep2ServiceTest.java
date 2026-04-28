package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginStep2Command;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.*;
import com.renewsim.backend.auth_service.application.result.LoginStep2Result;
import com.renewsim.backend.auth_service.application.validator.UserAccountValidator;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.model.RefreshToken;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.testutil.mothers.UserSnapshotMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LoginStep2Service")
class LoginStep2ServiceTest {

        @Mock
        private UserAccountGateway userAccountGateway;
        @Mock
        private OtpCodeRepositoryPort otpCodeRepositoryPort;
        @Mock
        private RefreshTokenRepositoryPort refreshTokenRepositoryPort;
        @Mock
        private TokenProvider tokenProvider;
        @Mock
        private PasswordEncoderPort passwordEncoderPort;
        @Mock
        private TransactionalPort transactionalPort;
        @Mock
        private UserAccountValidator userAccountValidator;

        private LoginStep2Service service;
        private Clock clock;

        @BeforeEach
        void setUp() {
                // Clock real fijo
                clock = Clock.fixed(
                                Instant.parse("2026-04-24T10:00:00Z"),
                                ZoneId.of("UTC"));

                // Constructor con 8 parámetros (SIN CredentialsValidator)
                service = new LoginStep2Service(
                                userAccountGateway,
                                otpCodeRepositoryPort,
                                refreshTokenRepositoryPort,
                                tokenProvider,
                                passwordEncoderPort,
                                transactionalPort,
                                clock,
                                userAccountValidator);

                // Configurar TransactionalPort SIEMPRE
                when(transactionalPort.execute(any())).thenAnswer(inv -> inv.getArgument(0, Supplier.class).get());

                // Default: no failed attempts
                when(otpCodeRepositoryPort.countFailedAttempts(any(), any()))
                                .thenReturn(0L);
        }

        @Test
        @DisplayName("OTP válido → devuelve access token y persiste refresh token")
        void execute_validOtp_returnsTokens() {
                // Given
                UserSnapshot activeUser = UserSnapshotMother.withEmail(
                                "john@example.com",
                                Set.of(RoleName.USER));
                OtpCode validOtp = OtpCode.issue(
                                activeUser.id(),
                                "$hashed_otp",
                                OtpCode.Purpose.LOGIN,
                                clock);

                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                doNothing().when(userAccountValidator).validateEnabledOrThrow(activeUser);
                when(otpCodeRepositoryPort.countFailedAttempts(any(), any()))
                                .thenReturn(0L);
                when(otpCodeRepositoryPort.findLatestValidByUserId(
                                activeUser.id(),
                                OtpCode.Purpose.LOGIN)).thenReturn(Optional.of(validOtp));
                when(passwordEncoderPort.matches("123456", "$hashed_otp"))
                                .thenReturn(true);
                when(tokenProvider.generate(any(AuthenticatedUser.class)))
                                .thenReturn("jwt-token");
                when(tokenProvider.expiresInSeconds()).thenReturn(3600L);
                when(otpCodeRepositoryPort.save(any(OtpCode.class)))
                                .thenAnswer(i -> i.getArgument(0));
                when(refreshTokenRepositoryPort.save(any(RefreshToken.class)))
                                .thenAnswer(i -> i.getArgument(0));

                // When
                LoginStep2Result result = service.execute(
                                new LoginStep2Command("john@example.com", "123456"));

                // Then
                assertThat(result.accessToken()).isEqualTo("jwt-token");
                assertThat(result.tokenType()).isEqualTo("Bearer");
                assertThat(result.expiresIn()).isEqualTo(3600L);
                assertThat(result.username()).isEqualTo("john@example.com");
                assertThat(result.roles()).contains("USER");
                assertThat(result.rawRefreshToken()).isNotNull().isNotBlank();

                // Verify OTP was saved
                verify(otpCodeRepositoryPort).save(any(OtpCode.class));
                verify(refreshTokenRepositoryPort).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("email desconocido → lanza AuthenticationException")
        void execute_unknownEmail_throws() {
                // Given
                when(userAccountGateway.findByEmail("unknown@example.com"))
                                .thenReturn(Optional.empty());

                // When / Then
                assertThatThrownBy(() -> service.execute(
                                new LoginStep2Command("unknown@example.com", "123456")))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("Invalid credentials");

                // Verify no side effects
                verifyNoInteractions(otpCodeRepositoryPort);
                verifyNoInteractions(refreshTokenRepositoryPort);
                verifyNoInteractions(tokenProvider);
        }

        @Test
        @DisplayName("usuario desactivado → lanza AuthenticationException")
        void execute_disabledUser_throws() {
                // Given
                UserSnapshot disabledUser = UserSnapshotMother.disabledUser(
                                "jane",
                                Set.of(RoleName.USER));

                when(userAccountGateway.findByEmail("jane@example.com"))
                                .thenReturn(Optional.of(disabledUser));
                doThrow(new AuthenticationException("Account is not active"))
                                .when(userAccountValidator)
                                .validateEnabledOrThrow(disabledUser);

                // When / Then
                assertThatThrownBy(() -> service.execute(
                                new LoginStep2Command("jane@example.com", "123456")))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("not active");

                // Verify no side effects
                verifyNoInteractions(otpCodeRepositoryPort);
                verifyNoInteractions(refreshTokenRepositoryPort);
                verifyNoInteractions(tokenProvider);
        }

        @Test
        @DisplayName("sin OTP válido → lanza AuthenticationException")
        void execute_noValidOtp_throws() {
                // Given
                UserSnapshot activeUser = UserSnapshotMother.withEmail(
                                "john@example.com",
                                Set.of(RoleName.USER));

                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                doNothing().when(userAccountValidator).validateEnabledOrThrow(activeUser);
                when(otpCodeRepositoryPort.findLatestValidByUserId(
                                activeUser.id(),
                                OtpCode.Purpose.LOGIN)).thenReturn(Optional.empty());

                // When / Then
                assertThatThrownBy(() -> service.execute(
                                new LoginStep2Command("john@example.com", "123456")))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("OTP");

                // Verify no tokens generated
                verifyNoInteractions(tokenProvider);
                verifyNoInteractions(refreshTokenRepositoryPort);
        }

        @Test
        @DisplayName("código OTP incorrecto → lanza AuthenticationException sin persistir")
        void execute_wrongOtpCode_throws() {
                // Given
                UserSnapshot activeUser = UserSnapshotMother.withEmail(
                                "john@example.com",
                                Set.of(RoleName.USER));
                OtpCode validOtp = OtpCode.issue(
                                activeUser.id(),
                                "$hashed_otp",
                                OtpCode.Purpose.LOGIN,
                                clock);

                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                doNothing().when(userAccountValidator).validateEnabledOrThrow(activeUser);
                when(otpCodeRepositoryPort.findLatestValidByUserId(
                                activeUser.id(),
                                OtpCode.Purpose.LOGIN)).thenReturn(Optional.of(validOtp));
                when(passwordEncoderPort.matches("wrongotp", "$hashed_otp"))
                                .thenReturn(false);

                // When / Then
                assertThatThrownBy(() -> service.execute(
                                new LoginStep2Command("john@example.com", "wrongotp")))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("OTP");

                // Verify no persistence
                verify(otpCodeRepositoryPort, never()).save(any());
                verify(refreshTokenRepositoryPort, never()).save(any());
                verifyNoInteractions(tokenProvider);
        }

        @Test
        @DisplayName("OTP válido → OTP queda marcado como usado")
        void execute_validOtp_otpMarkedUsed() {
                // Given
                UserSnapshot activeUser = UserSnapshotMother.withEmail(
                                "john@example.com",
                                Set.of(RoleName.USER));
                OtpCode validOtp = OtpCode.issue(
                                activeUser.id(),
                                "$hashed_otp",
                                OtpCode.Purpose.LOGIN,
                                clock);

                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                doNothing().when(userAccountValidator).validateEnabledOrThrow(activeUser);
                when(otpCodeRepositoryPort.countFailedAttempts(any(), any()))
                                .thenReturn(0L);
                when(otpCodeRepositoryPort.findLatestValidByUserId(
                                activeUser.id(),
                                OtpCode.Purpose.LOGIN)).thenReturn(Optional.of(validOtp));
                when(passwordEncoderPort.matches("123456", "$hashed_otp"))
                                .thenReturn(true);
                when(tokenProvider.generate(any())).thenReturn("jwt");
                when(tokenProvider.expiresInSeconds()).thenReturn(3600L);
                when(otpCodeRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));
                when(refreshTokenRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

                // When
                service.execute(new LoginStep2Command("john@example.com", "123456"));

                // Then
                verify(otpCodeRepositoryPort).save(any(OtpCode.class));
        }
}