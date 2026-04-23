package com.renewsim.backend.auth_service.application;

import com.renewsim.backend.auth_service.application.command.AuthCommand;
import com.renewsim.backend.auth_service.application.command.RegisterCommand;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.mapper.AuthResponseMapper;
import com.renewsim.backend.auth_service.application.port.out.ActivationTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.AuthResult;
import com.renewsim.backend.auth_service.application.result.RegisterResult;
import com.renewsim.backend.auth_service.application.service.AuthServiceImpl;
import com.renewsim.backend.auth_service.application.validator.CredentialsValidator;
import com.renewsim.backend.auth_service.domain.model.ActivationToken;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;
import com.renewsim.backend.testutil.mothers.UserSnapshotMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

        @Mock
        private UserAccountGateway userAccountGateway;
        @Mock
        private CredentialsValidator credentialsValidator;
        @Mock
        private AuthResponseMapper authResponseMapper;
        @Mock
        private ActivationTokenRepositoryPort activationTokenRepositoryPort;
        @Mock
        private EmailPort emailPort;

        private AuthServiceImpl authService;
        private UserSnapshot snapshot;

        @BeforeEach
        void setUp() {
                authService = new AuthServiceImpl(
                                userAccountGateway,
                                credentialsValidator,
                                authResponseMapper,
                                activationTokenRepositoryPort,
                                emailPort);
                snapshot = UserSnapshotMother.activeUser("john", Set.of(RoleName.USER));
        }

        // ─────────────────────────────────────────────────────
        // LOGIN
        // ─────────────────────────────────────────────────────

        @Test
        @DisplayName("login: credenciales válidas por username → devuelve AuthResult del mapper")
        void login_validUsername_returnsAuthResult() {
                AuthCommand command = new AuthCommand("john", "secret");

                AuthResult expectedResult = new AuthResult(
                                "jwt-token",
                                "Bearer",
                                Instant.now().plusSeconds(3600),
                                "john",
                                Set.of("USER"),
                                Set.of("read:simulations"));

                when(userAccountGateway.findByUsername("john")).thenReturn(Optional.of(snapshot));
                when(authResponseMapper.toAuthResult(snapshot)).thenReturn(expectedResult);

                AuthResult result = authService.login(command);

                assertThat(result).isNotNull();
                assertThat(result.username()).isEqualTo("john");
                assertThat(result.token()).isEqualTo("jwt-token");
                assertThat(result.roles()).containsExactly("USER");
                assertThat(result.scopes()).containsExactly("read:simulations");

                verify(credentialsValidator).validateCredentials("john", "secret");
                verify(credentialsValidator).validateUserEnabled(true);
                verify(credentialsValidator).validatePassword("secret", snapshot.passwordHash());
                verify(userAccountGateway).findByUsername("john");
                verify(authResponseMapper).toAuthResult(snapshot);
        }

        @Test
        @DisplayName("login: login con email → busca por email")
        void login_withEmail_searchesByEmail() {
                AuthCommand command = new AuthCommand("john@example.com", "secret");
                UserSnapshot emailSnapshot = UserSnapshotMother.withEmail("john@example.com", Set.of(RoleName.USER));

                AuthResult expectedResult = new AuthResult(
                                "jwt-token",
                                "Bearer",
                                Instant.now().plusSeconds(3600),
                                "john@example.com",
                                Set.of("USER"),
                                Set.of());

                when(userAccountGateway.findByEmail("john@example.com")).thenReturn(Optional.of(emailSnapshot));
                when(authResponseMapper.toAuthResult(emailSnapshot)).thenReturn(expectedResult);

                AuthResult result = authService.login(command);

                assertThat(result).isNotNull();
                assertThat(result.username()).isEqualTo("john@example.com");
                verify(userAccountGateway).findByEmail("john@example.com");
                verify(userAccountGateway, never()).findByUsername(any());
        }

        @Test
        @DisplayName("login: validateCredentials lanza excepción → no consulta BD")
        void login_invalidCredentials_throwsBeforeGateway() {
                AuthCommand command = new AuthCommand("john", "bad");
                doThrow(new AuthenticationException("Invalid credentials"))
                                .when(credentialsValidator).validateCredentials("john", "bad");

                assertThatThrownBy(() -> authService.login(command))
                                .isInstanceOf(AuthenticationException.class)
                                .hasMessageContaining("Invalid credentials");

                verifyNoInteractions(userAccountGateway);
                verifyNoInteractions(authResponseMapper);
        }

        @Test
        @DisplayName("login: usuario no encontrado → lanza AuthenticationException")
        void login_userNotFound_throwsAuthenticationException() {
                AuthCommand command = new AuthCommand("john", "secret");
                when(userAccountGateway.findByUsername("john")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> authService.login(command))
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
                RegisterCommand command = new RegisterCommand("John Doe", "SecurePass1!", "john@example.com");
                when(userAccountGateway.existsByEmail("john@example.com")).thenReturn(true);

                assertThatThrownBy(() -> authService.register(command))
                                .isInstanceOf(ResourceConflictException.class);

                verify(userAccountGateway).existsByEmail("john@example.com");
                verify(userAccountGateway, never()).createUser(any(), any(), any(), any(), any());
                verifyNoInteractions(activationTokenRepositoryPort);
                verifyNoInteractions(emailPort);
        }

        @Test
        @DisplayName("register: datos válidos → crea usuario con username derivado, persiste token y envía email")
        void register_validData_createsUserPersistsTokenAndSendsEmail() {
                RegisterCommand command = new RegisterCommand("John Doe", "SecurePass1!", "john@example.com");

                when(userAccountGateway.existsByEmail("john@example.com")).thenReturn(false);
                when(userAccountGateway.createUser(eq("john"), eq("John Doe"), eq("SecurePass1!"),
                                eq("john@example.com"), eq(Set.of(RoleName.USER))))
                                .thenReturn(snapshot);
                when(activationTokenRepositoryPort.save(any(ActivationToken.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                RegisterResult result = authService.register(command);

                assertThat(result).isNotNull();
                assertThat(result.email()).isEqualTo(snapshot.email());
                assertThat(result.fullName()).isEqualTo(snapshot.fullName());
                assertThat(result.message()).contains("registered successfully");

                verify(activationTokenRepositoryPort).save(any(ActivationToken.class));
                verify(emailPort).sendActivationEmail(eq(snapshot.email()), any(String.class));
        }

        @Test
        @DisplayName("register: username se deriva correctamente del email")
        void register_derivesUsernameFromEmail() {
                RegisterCommand command = new RegisterCommand("John Doe", "pass", "john@example.com");

                when(userAccountGateway.existsByEmail("john@example.com")).thenReturn(false);
                when(userAccountGateway.createUser(eq("john"), any(), any(), any(), any()))
                                .thenReturn(snapshot);
                when(activationTokenRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

                authService.register(command);

                verify(userAccountGateway).createUser(eq("john"), any(), any(), any(), any());
        }

        @Test
        @DisplayName("register: el token de activación tiene el userId correcto")
        void register_activationTokenHasCorrectUserId() {
                RegisterCommand command = new RegisterCommand("John Doe", "pass", "john@example.com");

                when(userAccountGateway.existsByEmail("john@example.com")).thenReturn(false);
                when(userAccountGateway.createUser(any(), any(), any(), any(), any())).thenReturn(snapshot);
                when(activationTokenRepositoryPort.save(any(ActivationToken.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                authService.register(command);

                verify(activationTokenRepositoryPort)
                                .save(argThat(token -> token.getUserId().equals(snapshot.id()) && !token.isUsed()));
        }
}