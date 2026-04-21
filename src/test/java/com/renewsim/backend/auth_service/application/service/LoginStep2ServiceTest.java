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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private LoginStep2Service service;

        private UserSnapshot activeUser;
        private UserSnapshot disabledUser;
        private OtpCode validOtp;

        @BeforeEach
        void setUp() {
                activeUser = UserSnapshotMother.withEmail("john@example.com", Set.of(RoleName.USER));
                disabledUser = UserSnapshotMother.disabledUser("jane", Set.of(RoleName.USER));
                validOtp = OtpCode.issue(1L, "$hashed_otp", OtpCode.Purpose.LOGIN);
        }

        @Test
        @DisplayName("OTP válido -> devuelve access token y persiste refresh token")
        void execute_validOtp_returnsTokens() {
                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                when(otpCodeRepositoryPort.findLatestValidByUserId(activeUser.id(), OtpCode.Purpose.LOGIN))
                                .thenReturn(Optional.of(validOtp));
                when(passwordEncoder.matches("123456", "$hashed_otp")).thenReturn(true);
                when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn("jwt-token");
                when(tokenProvider.expiresInSeconds()).thenReturn(3600L);
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
                assertThat(result.rawRefreshToken()).isNotNull().isNotBlank();
                assertThat(validOtp.isUsed()).isTrue();

                verify(otpCodeRepositoryPort).save(any(OtpCode.class));
                verify(refreshTokenRepositoryPort).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("email desconocido -> lanza AuthenticationException")
        void execute_unknownEmail_throws() {
                when(userAccountGateway.findByEmail("unknown@example.com"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.execute(
                                new LoginStep2Command("unknown@example.com", "123456")))
                                .isInstanceOf(AuthenticationException.class);
        }

        @Test
        @DisplayName("usuario desactivado -> lanza AuthenticationException")
        void execute_disabledUser_throws() {
                when(userAccountGateway.findByEmail("jane@example.com"))
                                .thenReturn(Optional.of(disabledUser));

                assertThatThrownBy(() -> service.execute(
                                new LoginStep2Command("jane@example.com", "123456")))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("sin OTP válido -> lanza AuthenticationException")
        void execute_noValidOtp_throws() {
                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                when(otpCodeRepositoryPort.findLatestValidByUserId(activeUser.id(), OtpCode.Purpose.LOGIN))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.execute(
                                new LoginStep2Command("john@example.com", "123456")))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("OTP");
        }

        @Test
        @DisplayName("código OTP incorrecto -> lanza AuthenticationException sin persistir")
        void execute_wrongOtpCode_throws() {
                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                when(otpCodeRepositoryPort.findLatestValidByUserId(activeUser.id(), OtpCode.Purpose.LOGIN))
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
        @DisplayName("OTP válido -> OTP queda marcado como usado")
        void execute_validOtp_otpMarkedUsed() {
                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));
                when(otpCodeRepositoryPort.findLatestValidByUserId(activeUser.id(), OtpCode.Purpose.LOGIN))
                                .thenReturn(Optional.of(validOtp));
                when(passwordEncoder.matches("123456", "$hashed_otp")).thenReturn(true);
                when(tokenProvider.generate(any())).thenReturn("jwt");
                when(tokenProvider.expiresInSeconds()).thenReturn(3600L);
                when(refreshTokenRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));
                when(otpCodeRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

                service.execute(new LoginStep2Command("john@example.com", "123456"));

                assertThat(validOtp.isUsed()).isTrue();
        }
}