package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.AuthCommand;
import com.renewsim.backend.auth_service.application.command.RegisterCommand;
import com.renewsim.backend.auth_service.application.result.AuthResult;
import com.renewsim.backend.auth_service.application.result.RegisterResult;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("AuthUseCase contract")
class AuthUseCaseTest {

    @Mock
    private AuthUseCase authUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("login: contrato acepta AuthCommand y devuelve AuthResult")
    void login_contract_returnsAuthResult() {
        AuthCommand command = new AuthCommand("john@example.com", "secret");
        AuthResult expected = new AuthResult(
                "jwt-token", "Bearer", Instant.now().plusSeconds(3600),
                "john@example.com", Set.of("USER"), Set.of("read:simulations"));

        when(authUseCase.login(command)).thenReturn(expected);

        AuthResult result = authUseCase.login(command);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("john@example.com");
        assertThat(result.token()).isEqualTo("jwt-token");
        verify(authUseCase).login(command);
    }

    @Test
    @DisplayName("register: contrato acepta RegisterCommand y devuelve RegisterResult")
    void register_contract_returnsRegisterResult() {
        RegisterCommand command = new RegisterCommand("John Doe", "SecurePass1!", "john@example.com");
        RegisterResult expected = new RegisterResult(
                1L, "john@example.com", "John Doe", UserStatus.INACTIVE,
                "User registered successfully. Please check your email to activate your account.");

        when(authUseCase.register(command)).thenReturn(expected);

        RegisterResult result = authUseCase.register(command);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.fullName()).isEqualTo("John Doe");
        assertThat(result.status()).isEqualTo(UserStatus.INACTIVE);
        assertThat(result.message()).contains("registered successfully");
        verify(authUseCase).register(command);
    }
}
