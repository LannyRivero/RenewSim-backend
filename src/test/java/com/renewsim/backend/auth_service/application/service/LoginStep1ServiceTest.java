package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginStep1Command;
import com.renewsim.backend.auth_service.application.port.out.OtpCodeRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LoginStep1ResultDTO;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.service.OtpGenerator;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;
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

    @BeforeEach
    void setUp() {
        activeUser = new UserSnapshot(
                1L, "john", "hashedpass", "john@example.com",
                Set.of(RoleName.USER), true);
    }

    @Test
    @DisplayName("valid credentials -> returns generic message and invalidates previous OTP")
    void execute_validCredentials_generatesOtp() {
        when(userAccountGateway.findByEmail("john@example.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("secret", "hashedpass")).thenReturn(true);
        when(otpGenerator.generate()).thenReturn("123456");
        when(passwordEncoder.encode("123456")).thenReturn("$hashed_otp");
        when(otpCodeRepositoryPort.save(any(OtpCode.class))).thenAnswer(i -> i.getArgument(0));

        LoginStep1ResultDTO result = service.execute(
                new LoginStep1Command("john@example.com", "secret"));

        assertThat(result.message()).contains("If your account exists");
        assertThat(result.expiresInSeconds()).isEqualTo(300);

        verify(otpCodeRepositoryPort).invalidateAllByUserId(1L, OtpCode.Purpose.LOGIN);
        verify(otpCodeRepositoryPort).save(any(OtpCode.class));
        verify(otpGenerator).generate();
        verify(passwordEncoder).encode("123456");
    }

    @Test
    @DisplayName("unknown email -> returns generic message without generating OTP")
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
    @DisplayName("disabled user -> returns generic message without generating OTP")
    void execute_disabledUser_returnsGenericMessage() {
        UserSnapshot disabled = new UserSnapshot(
                2L, "jane", "hashedpass", "jane@example.com",
                Set.of(RoleName.USER), false);

        when(userAccountGateway.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(disabled));

        LoginStep1ResultDTO result = service.execute(
                new LoginStep1Command("jane@example.com", "secret"));

        assertThat(result.message()).contains("If your account exists");
        verifyNoInteractions(otpGenerator);
        verifyNoInteractions(otpCodeRepositoryPort);
    }

    @Test
    @DisplayName("wrong password -> returns generic message without generating OTP")
    void execute_wrongPassword_returnsGenericMessage() {
        when(userAccountGateway.findByEmail("john@example.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongpass", "hashedpass")).thenReturn(false);

        LoginStep1ResultDTO result = service.execute(
                new LoginStep1Command("john@example.com", "wrongpass"));

        assertThat(result.message()).contains("If your account exists");
        verifyNoInteractions(otpGenerator);
        verifyNoInteractions(otpCodeRepositoryPort);
    }

    @Test
    @DisplayName("valid credentials -> expiresInSeconds is 300")
    void execute_validCredentials_expiresIn300Seconds() {
        when(userAccountGateway.findByEmail("john@example.com"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("secret", "hashedpass")).thenReturn(true);
        when(otpGenerator.generate()).thenReturn("654321");
        when(passwordEncoder.encode("654321")).thenReturn("$hashed_otp2");
        when(otpCodeRepositoryPort.save(any(OtpCode.class))).thenAnswer(i -> i.getArgument(0));

        LoginStep1ResultDTO result = service.execute(
                new LoginStep1Command("john@example.com", "secret"));

        assertThat(result.expiresInSeconds()).isEqualTo(300);
    }
}