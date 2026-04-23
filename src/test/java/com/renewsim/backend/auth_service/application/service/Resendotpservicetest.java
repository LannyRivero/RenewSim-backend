package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.ResendOtpCommand;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.OtpCodeRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.PasswordEncoderPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.ResendOtpResult;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.service.OtpGenerator;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.testutil.mothers.UserSnapshotMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResendOtpService")
class ResendOtpServiceTest {

    @Mock
    private UserAccountGateway userAccountGateway;
    @Mock
    private OtpCodeRepositoryPort otpCodeRepositoryPort;
    @Mock
    private OtpGenerator otpGenerator;
    @Mock
    private PasswordEncoderPort passwordEncoder;
    @Mock
    private EmailPort emailPort;

    @InjectMocks
    private ResendOtpService service;

    private UserSnapshot activeUser;

    @BeforeEach
    void setUp() {
        activeUser = UserSnapshotMother.withEmail("john@example.com", Set.of(RoleName.USER));
    }

    @Test
    @DisplayName("email válido → invalida OTP previo, genera nuevo y envía email")
    void execute_validEmail_resetsOtpAndSendsEmail() {
        when(userAccountGateway.findByEmail("john@example.com")).thenReturn(Optional.of(activeUser));
        when(otpGenerator.generate()).thenReturn("111222");
        when(passwordEncoder.encode("111222")).thenReturn("$hashed");
        when(otpCodeRepositoryPort.save(any(OtpCode.class))).thenAnswer(i -> i.getArgument(0));

        ResendOtpResult result = service.execute(new ResendOtpCommand("john@example.com"));

        assertThat(result.message()).contains("If your account exists");
        assertThat(result.expiresInSeconds()).isEqualTo(300);

        verify(otpCodeRepositoryPort).invalidateAllByUserId(activeUser.id(), OtpCode.Purpose.LOGIN);
        verify(otpCodeRepositoryPort).save(any(OtpCode.class));
        verify(emailPort).sendOtp(eq(activeUser.email()), eq("111222"), eq(300));
    }

    @Test
    @DisplayName("email desconocido → respuesta genérica, sin OTP ni email")
    void execute_unknownEmail_returnsGenericWithoutEmail() {
        when(userAccountGateway.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        ResendOtpResult result = service.execute(new ResendOtpCommand("ghost@example.com"));

        assertThat(result.message()).contains("If your account exists");
        verifyNoInteractions(otpGenerator, otpCodeRepositoryPort, emailPort);
    }

    @Test
    @DisplayName("usuario desactivado → respuesta genérica, sin OTP ni email")
    void execute_disabledUser_returnsGenericWithoutEmail() {
        UserSnapshot disabled = UserSnapshotMother.disabledUser("jane", Set.of(RoleName.USER));
        when(userAccountGateway.findByEmail("jane@example.com")).thenReturn(Optional.of(disabled));

        ResendOtpResult result = service.execute(new ResendOtpCommand("jane@example.com"));

        assertThat(result.message()).contains("If your account exists");
        verifyNoInteractions(otpGenerator, otpCodeRepositoryPort, emailPort);
    }

    @Test
    @DisplayName("email válido → sendOtp recibe TTL de 300 segundos")
    void execute_validEmail_emailPortReceives300SecondTtl() {
        when(userAccountGateway.findByEmail("john@example.com")).thenReturn(Optional.of(activeUser));
        when(otpGenerator.generate()).thenReturn("333444");
        when(passwordEncoder.encode("333444")).thenReturn("$hash");
        when(otpCodeRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

        service.execute(new ResendOtpCommand("john@example.com"));

        verify(emailPort).sendOtp(any(), any(), eq(300));
    }
}