package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginStep1Command;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.*;
import com.renewsim.backend.auth_service.application.result.LoginStep1Result;
import com.renewsim.backend.auth_service.application.validator.CredentialsValidator;
import com.renewsim.backend.auth_service.application.validator.UserAccountValidator;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.service.OtpGenerator;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginStep1Service")
class LoginStep1ServiceTest {

        @Mock
        private UserAccountGateway userAccountGateway;
        @Mock
        private OtpCodeRepositoryPort otpCodeRepositoryPort;
        @Mock
        private OtpGenerator otpGenerator;
        @Mock
        private CredentialsValidator credentialsValidator;
        @Mock
        private PasswordEncoderPort passwordEncoder;
        @Mock
        private EmailPort emailPort;
        @Mock
        private TransactionalPort transactionalPort;
        @Mock
        private UserAccountValidator userAccountValidator;

        private LoginStep1Service service;
        private Clock clock;

        @BeforeEach
        void setUp() {
                // Clock real fijo para tests determinísticos
                clock = Clock.fixed(
                                Instant.parse("2026-04-24T10:00:00Z"),
                                ZoneId.of("UTC"));

                service = new LoginStep1Service(
                                userAccountGateway,
                                otpCodeRepositoryPort,
                                otpGenerator,
                                credentialsValidator,
                                passwordEncoder,
                                emailPort,
                                transactionalPort,
                                clock,
                                userAccountValidator);

                // Configurar TransactionalPort para ejecutar la lambda directamente
                when(transactionalPort.execute(any())).thenAnswer(inv -> inv.getArgument(0, Supplier.class).get());
        }

        @Test
        @DisplayName("credenciales válidas → genera OTP, persiste y envía email")
        void execute_validCredentials_generatesOtpAndSendsEmail() {
                // Given
                Long userId = 1L;
                UserSnapshot user = UserSnapshot.active(
                                userId,
                                "testuser",
                                "Test User",
                                "hashedPassword",
                                "test@example.com",
                                Set.of(RoleName.USER));

                when(userAccountGateway.findByEmail("test@example.com"))
                                .thenReturn(Optional.of(user));
                when(userAccountValidator.isEnabled(user)).thenReturn(true);
                doNothing().when(credentialsValidator)
                                .validatePassword("password123", "hashedPassword");
                when(otpGenerator.generate()).thenReturn("123456");
                when(passwordEncoder.encode("123456")).thenReturn("hashedOtp");

                LoginStep1Command command = new LoginStep1Command(
                                "test@example.com",
                                "password123");

                // When
                LoginStep1Result result = service.execute(command);

                // Then
                assertThat(result.message())
                                .isEqualTo("If your account exists and is active, you will receive an OTP.");
                assertThat(result.expiresInSeconds()).isEqualTo(300);

                // Verify OTP invalidation
                verify(otpCodeRepositoryPort)
                                .invalidateAllByUserId(userId, OtpCode.Purpose.LOGIN);

                // Verify OTP persistence
                ArgumentCaptor<OtpCode> otpCaptor = ArgumentCaptor.forClass(OtpCode.class);
                verify(otpCodeRepositoryPort).save(otpCaptor.capture());

                OtpCode savedOtp = otpCaptor.getValue();
                assertThat(savedOtp.getUserId()).isEqualTo(userId);
                assertThat(savedOtp.getCodeHash()).isEqualTo("hashedOtp");
                assertThat(savedOtp.getPurpose()).isEqualTo(OtpCode.Purpose.LOGIN);
                assertThat(savedOtp.isUsed()).isFalse();

                // Verify email sent with raw OTP
                verify(emailPort).sendOtp("test@example.com", "123456", 300);
        }

        @Test
        @DisplayName("email desconocido → respuesta genérica sin generar OTP ni enviar email")
        void execute_unknownEmail_returnsGenericMessageWithoutEmail() {
                // Given
                when(userAccountGateway.findByEmail("unknown@example.com"))
                                .thenReturn(Optional.empty());

                LoginStep1Command command = new LoginStep1Command(
                                "unknown@example.com",
                                "anyPassword");

                // When
                LoginStep1Result result = service.execute(command);

                // Then
                assertThat(result.message())
                                .isEqualTo("If your account exists and is active, you will receive an OTP.");
                assertThat(result.expiresInSeconds()).isEqualTo(300);

                // Verify no OTP operations
                verifyNoInteractions(otpCodeRepositoryPort);
                verifyNoInteractions(otpGenerator);
                verifyNoInteractions(passwordEncoder);
                verifyNoInteractions(emailPort);
        }

        @Test
        @DisplayName("usuario desactivado → respuesta genérica sin generar OTP ni enviar email")
        void execute_disabledUser_returnsGenericMessageWithoutEmail() {
                // Given
                UserSnapshot disabledUser = UserSnapshot.disabled(
                                1L,
                                "disableduser",
                                "Disabled User",
                                "hashedPassword",
                                "disabled@example.com",
                                Set.of(RoleName.USER));

                when(userAccountGateway.findByEmail("disabled@example.com"))
                                .thenReturn(Optional.of(disabledUser));
                when(userAccountValidator.isEnabled(disabledUser)).thenReturn(false);

                LoginStep1Command command = new LoginStep1Command(
                                "disabled@example.com",
                                "password123");

                // When
                LoginStep1Result result = service.execute(command);

                // Then
                assertThat(result.message())
                                .isEqualTo("If your account exists and is active, you will receive an OTP.");

                // Verify no OTP operations
                verifyNoInteractions(otpCodeRepositoryPort);
                verifyNoInteractions(otpGenerator);
                verifyNoInteractions(passwordEncoder);
                verifyNoInteractions(emailPort);
        }

        @Test
        @DisplayName("password incorrecta → respuesta genérica sin generar OTP ni enviar email")
        void execute_wrongPassword_returnsGenericMessageWithoutEmail() {
                // Given
                UserSnapshot user = UserSnapshot.active(
                                1L,
                                "testuser",
                                "Test User",
                                "hashedPassword",
                                "test@example.com",
                                Set.of(RoleName.USER));

                when(userAccountGateway.findByEmail("test@example.com"))
                                .thenReturn(Optional.of(user));
                when(userAccountValidator.isEnabled(user)).thenReturn(true);
                doThrow(new AuthenticationException("Invalid password"))
                                .when(credentialsValidator)
                                .validatePassword("wrongPassword", "hashedPassword");

                LoginStep1Command command = new LoginStep1Command(
                                "test@example.com",
                                "wrongPassword");

                // When
                LoginStep1Result result = service.execute(command);

                // Then
                assertThat(result.message())
                                .isEqualTo("If your account exists and is active, you will receive an OTP.");

                // Verify no OTP operations
                verifyNoInteractions(otpCodeRepositoryPort);
                verifyNoInteractions(otpGenerator);
                verifyNoInteractions(passwordEncoder);
                verifyNoInteractions(emailPort);
        }

        @Test
        @DisplayName("credenciales válidas → expiresInSeconds es 300")
        void execute_validCredentials_expiresIn300Seconds() {
                // Given
                UserSnapshot user = UserSnapshot.active(
                                1L,
                                "testuser",
                                "Test User",
                                "hashedPassword",
                                "test@example.com",
                                Set.of(RoleName.USER));

                when(userAccountGateway.findByEmail("test@example.com"))
                                .thenReturn(Optional.of(user));
                when(userAccountValidator.isEnabled(user)).thenReturn(true);
                doNothing().when(credentialsValidator)
                                .validatePassword(any(), any());
                when(otpGenerator.generate()).thenReturn("123456");
                when(passwordEncoder.encode(any())).thenReturn("hashedOtp");

                LoginStep1Command command = new LoginStep1Command(
                                "test@example.com",
                                "password123");

                // When
                LoginStep1Result result = service.execute(command);

                // Then
                assertThat(result.expiresInSeconds()).isEqualTo(300);
        }

        @Test
        @DisplayName("credenciales válidas → sendOtp recibe el OTP correcto y TTL de 300s")
        void execute_validCredentials_emailPortReceivesCorrectOtpAndTtl() {
                // Given
                UserSnapshot user = UserSnapshot.active(
                                1L,
                                "testuser",
                                "Test User",
                                "hashedPassword",
                                "test@example.com",
                                Set.of(RoleName.USER));

                when(userAccountGateway.findByEmail("test@example.com"))
                                .thenReturn(Optional.of(user));
                when(userAccountValidator.isEnabled(user)).thenReturn(true);
                doNothing().when(credentialsValidator)
                                .validatePassword("password123", "hashedPassword");
                when(otpGenerator.generate()).thenReturn("654321"); // OTP específico
                when(passwordEncoder.encode("654321")).thenReturn("hashedOtp");

                LoginStep1Command command = new LoginStep1Command(
                                "test@example.com",
                                "password123");

                // When
                service.execute(command);

                // Then
                verify(emailPort).sendOtp("test@example.com", "654321", 300);
        }
}