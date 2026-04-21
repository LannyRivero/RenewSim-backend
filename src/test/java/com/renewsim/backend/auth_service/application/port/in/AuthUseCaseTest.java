package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterRequestDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterResponseDTO;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    @DisplayName("login: contrato devuelve AuthResponseDTO")
    void login_contract_returnsAuthResponseDTO() {
        AuthRequestDTO request = new AuthRequestDTO("john@example.com", "secret");
        AuthResponseDTO expected = AuthResponseDTO.builder()
                .username("john@example.com")
                .token("jwt-token")
                .roles(Set.of("USER"))
                .scopes(Set.of("read:simulations"))
                .build();

        when(authUseCase.login(request)).thenReturn(expected);

        AuthResponseDTO result = authUseCase.login(request);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john@example.com");
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(authUseCase).login(request);
    }

    @Test
    @DisplayName("register: contrato devuelve RegisterResponseDTO")
    void register_contract_returnsRegisterResponseDTO() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "john@example.com", "SecurePass1!", "John Doe");
        RegisterResponseDTO expected = new RegisterResponseDTO(
                1L,
                "john@example.com",
                "John Doe",
                UserStatus.INACTIVE,
                "User registered successfully. Please check your email to activate your account.");

        when(authUseCase.register(request)).thenReturn(expected);

        RegisterResponseDTO result = authUseCase.register(request);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.fullName()).isEqualTo("John Doe");
        assertThat(result.status()).isEqualTo(UserStatus.INACTIVE);
        assertThat(result.message()).contains("registered successfully");
        verify(authUseCase).register(request);
    }
}