package com.renewsim.backend.auth_service.application;

import com.renewsim.backend.auth_service.application.mapper.AuthResponseMapper;
import com.renewsim.backend.auth_service.application.port.out.ActivationTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.service.AuthServiceImpl;
import com.renewsim.backend.auth_service.domain.AuthValidator;
import com.renewsim.backend.auth_service.domain.model.ActivationToken;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterRequestDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterResponseDTO;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;
import com.renewsim.backend.testutil.UnitTestBase;
import com.renewsim.backend.testutil.mothers.UserSnapshotMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest extends UnitTestBase {

    @Mock private UserAccountGateway userAccountGateway;
    @Mock private AuthValidator authValidator;
    @Mock private AuthResponseMapper authResponseMapper;
    @Mock private ActivationTokenRepositoryPort activationTokenRepositoryPort;
    @Mock private EmailPort emailPort;

    private AuthServiceImpl authService;
    private UserSnapshot snapshot;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userAccountGateway,
                authValidator,
                authResponseMapper,
                activationTokenRepositoryPort,
                emailPort);
        snapshot = UserSnapshotMother.activeUser("john", Set.of(RoleName.USER));
    }

    // ─────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("login: credenciales válidas → devuelve AuthResponseDTO del mapper")
    void login_validCredentials_returnsAuthResponseDTO() {
        AuthRequestDTO request = new AuthRequestDTO("john", "secret");

        when(userAccountGateway.findByUsername("john")).thenReturn(Optional.of(snapshot));
        when(authResponseMapper.toAuthResponseDTO(snapshot))
                .thenReturn(AuthResponseDTO.builder()
                        .username("john")
                        .token("jwt-token")
                        .roles(Set.of("USER"))
                        .scopes(Set.of("read:simulations"))
                        .build());

        AuthResponseDTO result = authService.login(request);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john");
        assertThat(result.getToken()).isEqualTo("jwt-token");

        verify(authValidator).validateCredentials(request);
        verify(authValidator).validateUserEnable(true);
        verify(userAccountGateway).findByUsername("john");
        verify(authResponseMapper).toAuthResponseDTO(snapshot);
    }

    @Test
    @DisplayName("login: login con email → busca por email")
    void login_withEmail_searchesByEmail() {
        AuthRequestDTO request = new AuthRequestDTO("john@example.com", "secret");
        UserSnapshot emailSnapshot = UserSnapshotMother.withEmail("john@example.com", Set.of(RoleName.USER));

        when(userAccountGateway.findByEmail("john@example.com")).thenReturn(Optional.of(emailSnapshot));
        when(authResponseMapper.toAuthResponseDTO(emailSnapshot))
                .thenReturn(AuthResponseDTO.builder()
                        .username("john@example.com")
                        .token("jwt-token")
                        .roles(Set.of("USER"))
                        .scopes(Set.of())
                        .build());

        AuthResponseDTO result = authService.login(request);

        assertThat(result).isNotNull();
        verify(userAccountGateway).findByEmail("john@example.com");
        verify(userAccountGateway, never()).findByUsername(any());
    }

    @Test
    @DisplayName("login: validateCredentials lanza excepción → no consulta BD")
    void login_invalidCredentials_throwsBeforeGateway() {
        AuthRequestDTO request = new AuthRequestDTO("john", "bad");
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authValidator).validateCredentials(request);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");

        verifyNoInteractions(userAccountGateway);
        verifyNoInteractions(authResponseMapper);
    }

    @Test
    @DisplayName("login: usuario no encontrado → lanza AuthenticationException")
    void login_userNotFound_throwsAuthenticationException() {
        AuthRequestDTO request = new AuthRequestDTO("john", "secret");
        when(userAccountGateway.findByUsername("john")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class);

        verify(userAccountGateway).findByUsername("john");
        verifyNoInteractions(authResponseMapper);
    }

    // ─────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────

    @Test
    @DisplayName("register: email ya existe → lanza ResourceConflictException")
    void register_emailAlreadyExists_throwsConflict() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "john@example.com", "SecurePass1!", "John Doe");

        when(userAccountGateway.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Email already registered");

        verify(userAccountGateway).existsByEmail("john@example.com");
        verify(userAccountGateway, never()).createUser(any(), any(), any(), any());
        verifyNoInteractions(authResponseMapper);
        verifyNoInteractions(activationTokenRepositoryPort);
        verifyNoInteractions(emailPort);
    }

    @Test
    @DisplayName("register: datos válidos → crea usuario, persiste token y envía email")
    void register_validData_createsUserPersistsTokenAndSendsEmail() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "john@example.com", "SecurePass1!", "John Doe");

        when(userAccountGateway.existsByEmail("john@example.com")).thenReturn(false);
        when(userAccountGateway.createUser("John Doe", "SecurePass1!",
                "john@example.com", Set.of(RoleName.USER)))
                .thenReturn(snapshot);
        when(activationTokenRepositoryPort.save(any(ActivationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RegisterResponseDTO result = authService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(snapshot.email());
        assertThat(result.fullName()).isEqualTo(snapshot.fullName());
        assertThat(result.status()).isEqualTo(snapshot.status());
        assertThat(result.message()).contains("registered successfully");

        verify(userAccountGateway).existsByEmail("john@example.com");
        verify(userAccountGateway).createUser("John Doe", "SecurePass1!",
                "john@example.com", Set.of(RoleName.USER));
        verify(activationTokenRepositoryPort).save(any(ActivationToken.class));
        verify(emailPort).sendActivationEmail(eq(snapshot.email()), any(String.class));
        verifyNoInteractions(authResponseMapper);
    }

    @Test
    @DisplayName("register: el token de activación que se persiste tiene el userId correcto")
    void register_validData_activationTokenHasCorrectUserId() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "john@example.com", "SecurePass1!", "John Doe");

        when(userAccountGateway.existsByEmail("john@example.com")).thenReturn(false);
        when(userAccountGateway.createUser(any(), any(), any(), any())).thenReturn(snapshot);
        when(activationTokenRepositoryPort.save(any(ActivationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        verify(activationTokenRepositoryPort)
                .save(argThat(token -> token.getUserId().equals(snapshot.id()) && !token.isUsed()));
    }

    @Test
    @DisplayName("register: sendActivationEmail recibe email del usuario y un token no vacío")
    void register_validData_emailPortReceivesCorrectEmailAndNonBlankToken() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "john@example.com", "SecurePass1!", "John Doe");

        when(userAccountGateway.existsByEmail("john@example.com")).thenReturn(false);
        when(userAccountGateway.createUser(any(), any(), any(), any())).thenReturn(snapshot);
        when(activationTokenRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        verify(emailPort).sendActivationEmail(
                eq(snapshot.email()),
                argThat(token -> token != null && !token.isBlank()));
    }

    @Test
    @DisplayName("register: no llama a validateCredentials (validación delegada a @Valid)")
    void register_doesNotCallValidateCredentials() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "john@example.com", "SecurePass1!", "John Doe");

        when(userAccountGateway.existsByEmail("john@example.com")).thenReturn(false);
        when(userAccountGateway.createUser(any(), any(), any(), any())).thenReturn(snapshot);
        when(activationTokenRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        verify(authValidator, never()).validateCredentials(any());
    }
}