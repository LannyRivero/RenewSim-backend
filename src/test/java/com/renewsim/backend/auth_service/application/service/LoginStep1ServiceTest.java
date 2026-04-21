package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginStep1Command;
import com.renewsim.backend.auth_service.application.port.out.OtpCodeRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LoginStep1ResultDTO;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.service.OtpGenerator;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.testutil.mothers.UserSnapshotMother;
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

import static org.assertj.core.api.Assertions.assertThat;
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
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private LoginStep1Service service;

        private UserSnapshot activeUser;
        private UserSnapshot disabledUser;

        @BeforeEach
        void setUp() {
                activeUser = UserSnapshotMother.withEmail("john@example.com", Set.of(RoleName.USER));
                disabledUser = UserSnapshotMother.disabledUser("jane", Set.of(RoleName.USER));
        }

        @Test
        @DisplayName("credenciales válidas -> mensaje genérico e invalida OTP anterior")
        void execute_validCredentials_generatesOtp() {
                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                when(passwordEncoder.matches("secret", activeUser.passwordHash())).thenReturn(true);
                when(otpGenerator.generate()).thenReturn("123456");
                when(passwordEncoder.encode("123456")).thenReturn("$hashed_otp");
                when(otpCodeRepositoryPort.save(any(OtpCode.class))).thenAnswer(i -> i.getArgument(0));

                LoginStep1ResultDTO result = service.execute(
                                new LoginStep1Command("john@example.com", "secret"));

                assertThat(result.message()).contains("If your account exists");
                assertThat(result.expiresInSeconds()).isEqualTo(300);

                verify(otpCodeRepositoryPort).invalidateAllByUserId(activeUser.id(), OtpCode.Purpose.LOGIN);
                verify(otpCodeRepositoryPort).save(any(OtpCode.class));
                verify(otpGenerator).generate();
                verify(passwordEncoder).encode("123456");
        }

        @Test
        @DisplayName("email desconocido -> mensaje genérico sin generar OTP")
        void execute_unknownEmail_returnsGenericMessage() {
                when(userAccountGateway.findByEmail("unknown@example.com"))
                                .thenReturn(Optional.empty());

                LoginStep1ResultDTO result = service.execute(
                                new LoginStep1Command("unknown@example.com", "secret"));

                assertThat(result.message()).contains("If your account exists");
                verifyNoInteractions(otpGenerator);
                verifyNoInteractions(otpCodeRepositoryPort);
        }

        @Test
        @DisplayName("usuario desactivado -> mensaje genérico sin generar OTP")
        void execute_disabledUser_returnsGenericMessage() {
                when(userAccountGateway.findByEmail("jane@example.com"))
                                .thenReturn(Optional.of(disabledUser));

                LoginStep1ResultDTO result = service.execute(
                                new LoginStep1Command("jane@example.com", "secret"));

                assertThat(result.message()).contains("If your account exists");
                verifyNoInteractions(otpGenerator);
                verifyNoInteractions(otpCodeRepositoryPort);
        }

        @Test
        @DisplayName("password incorrecta -> mensaje genérico sin generar OTP")
        void execute_wrongPassword_returnsGenericMessage() {
                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                when(passwordEncoder.matches("wrongpass", activeUser.passwordHash())).thenReturn(false);

                LoginStep1ResultDTO result = service.execute(
                                new LoginStep1Command("john@example.com", "wrongpass"));

                assertThat(result.message()).contains("If your account exists");
                verifyNoInteractions(otpGenerator);
                verifyNoInteractions(otpCodeRepositoryPort);
        }

        @Test
        @DisplayName("credenciales válidas -> expiresInSeconds es 300")
        void execute_validCredentials_expiresIn300Seconds() {
                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                when(passwordEncoder.matches("secret", activeUser.passwordHash())).thenReturn(true);
                when(otpGenerator.generate()).thenReturn("654321");
                when(passwordEncoder.encode("654321")).thenReturn("$hashed_otp2");
                when(otpCodeRepositoryPort.save(any(OtpCode.class))).thenAnswer(i -> i.getArgument(0));

                LoginStep1ResultDTO result = service.execute(
                                new LoginStep1Command("john@example.com", "secret"));

                assertThat(result.expiresInSeconds()).isEqualTo(300);
        }
}