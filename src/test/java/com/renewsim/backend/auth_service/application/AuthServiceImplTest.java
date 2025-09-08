package com.renewsim.backend.auth_service.application;

import com.renewsim.backend.auth_service.application.port.out.RoleProvider;
import com.renewsim.backend.auth_service.application.port.out.ScopePolicy;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.service.AuthServiceImpl;
import com.renewsim.backend.auth_service.domain.AuthValidator;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.TokenTimeService;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;
import com.renewsim.backend.shared.exception.ResourceConflictException;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.testutil.UnitTestBase;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest extends UnitTestBase {

    @Mock private UserAccountGateway userAccountGateway;
    @Mock private TokenProvider tokenProvider;
    @Mock private RoleProvider roleProvider;
    @Mock private ScopePolicy scopePolicy;
    @Mock private AuthValidator authValidator;
    @Mock private TokenTimeService tokenTimeService;

    private AuthServiceImpl authService;

    private Instant baseInstant;

    private AuthRequestDTO loginReq;
    private AuthenticatedUser john;
    private UserSnapshot snapshot;

    @BeforeEach
    void setUp() {
        baseInstant = Instant.parse("2025-01-01T00:00:00Z");

        authService = new AuthServiceImpl(
                userAccountGateway,
                tokenProvider,
                scopePolicy,
                authValidator,
                tokenTimeService
        );


        loginReq = new AuthRequestDTO("john", "secret");
        john = new AuthenticatedUser("john", Set.of("USER"), Set.of("simulation:read"));
        snapshot = new UserSnapshot().active("john", "abcdefgHashed", Set.of(RoleName.USER));
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(tokenProvider, roleProvider, userAccountGateway);
    }

    @Test
    @DisplayName("login → returns AuthResponseDTO with token when credentials are valid")
    void login_ok() {
        when(userAccountGateway.findByUsername("john")).thenReturn(Optional.of(snapshot));
        when(scopePolicy.scopesFor(RoleName.USER)).thenReturn(Set.of("read"));
        when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn("jwt-token");
        when(tokenProvider.expiresInSeconds()).thenReturn(3600L);

        AuthResponseDTO res = authService.login(loginReq);

        assertThat(res.getUsername()).isEqualTo("john");
        assertThat(res.getToken()).isEqualTo("jwt-token");
        assertThat(res.getRoles()).containsExactlyInAnyOrder("USER");
        assertThat(res.getScopes()).containsExactlyInAnyOrder("read");
        // Determinista con Clock.fixed:
        assertThat(res.getExpiresAt()).isEqualTo(baseInstant.plusSeconds(3600));

        verify(userAccountGateway).findByUsername("john");
        verify(scopePolicy).scopesFor(RoleName.USER);
        verify(tokenProvider).generate(argThat(au ->
                au.username().equals("john") &&
                au.roles().contains("USER") &&
                au.scopes().contains("read")));
        verify(tokenProvider).expiresInSeconds();
    }

    @Test
    @DisplayName("login → throws on invalid credentials")
    void login_invalid() {
        when(userAccountGateway.findByUsername("john")).thenReturn(Optional.of(snapshot));

        assertThatThrownBy(() -> authService.login(new AuthRequestDTO("john", "bad")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");

        verify(userAccountGateway).findByUsername("john");
        verifyNoInteractions(scopePolicy, tokenProvider, roleProvider);
    }

    @Test
    @DisplayName("register → throws when username already exists")
    void register_username_exists() {
        when(userAccountGateway.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new AuthRequestDTO("john", "secret")))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Username already exists");

        verify(userAccountGateway).existsByUsername("john");
        verifyNoInteractions(roleProvider, scopePolicy, userAccountGateway, tokenProvider);
    }

    @Test
    @DisplayName("register → creates user with default role, builds token and scopes deterministically")
    void register_ok() {
        when(userAccountGateway.existsByUsername("john")).thenReturn(false);
        when(roleProvider.defaultRole()).thenReturn(RoleName.USER);
        when(scopePolicy.scopesFor(RoleName.USER)).thenReturn(Set.of("simulation:read"));
        when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn("jwt-token");
        when(tokenProvider.expiresInSeconds()).thenReturn(3600L);

        var res = authService.register(new AuthRequestDTO("john", "secret"));

        assertThat(res.getUsername()).isEqualTo("john");
        assertThat(res.getToken()).isEqualTo("jwt-token");
        assertThat(res.getRoles()).containsExactly("USER");
        assertThat(res.getScopes()).containsExactly("simulation:read");
        assertThat(res.getExpiresAt()).isEqualTo(baseInstant.plusSeconds(3600));

        verify(userAccountGateway).existsByUsername("john");
        verify(roleProvider).defaultRole();
        verify(userAccountGateway).createUser("john", "$2a$10$hash", Set.of(RoleName.USER));
        verify(scopePolicy).scopesFor(RoleName.USER);
        verify(tokenProvider).generate(any(AuthenticatedUser.class));
        verify(tokenProvider).expiresInSeconds();
    }
}
