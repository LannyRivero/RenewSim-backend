package com.renewsim.backend.auth_service.application;

import com.renewsim.backend.auth_service.application.service.AuthServiceImpl;
import com.renewsim.backend.auth_service.application.mapper.AuthResponseMapper;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.domain.AuthValidator;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;
import com.renewsim.backend.testutil.UnitTestBase;
import com.renewsim.backend.testutil.mothers.UserSnapshotMother;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest extends UnitTestBase {

    @Mock private UserAccountGateway userAccountGateway;
    @Mock private AuthValidator authValidator;
    @Mock private AuthResponseMapper authResponseMapper;

    private AuthServiceImpl authService;
    private AuthRequestDTO loginReq;
    private UserSnapshot snapshot;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userAccountGateway, authValidator, authResponseMapper);
        loginReq = new AuthRequestDTO("john", "secret");
        snapshot = UserSnapshotMother.activeUser("john", Set.of(RoleName.USER));
    }

    @Test
    @DisplayName("login → returns AuthResponseDTO from mapper when credentials are valid")
    void login_ok() {
        when(userAccountGateway.findByUsername("john")).thenReturn(Optional.of(snapshot));
        when(authResponseMapper.toAuthResponseDTO(any(UserSnapshot.class)))
                .thenReturn(AuthResponseDTO.builder()
                        .username("john")
                        .token("jwt-token")
                        .roles(Set.of("USER"))
                        .scopes(Set.of("read"))
                        .build());

        AuthResponseDTO res = authService.login(loginReq);

        assertThat(res).isNotNull();
        assertThat(res.getUsername()).isEqualTo("john");
        assertThat(res.getToken()).isEqualTo("jwt-token");

        verify(authValidator).validateCredentials(loginReq);
        verify(authValidator).validateUserEnable(true);
        verify(userAccountGateway).findByUsername("john");
        verify(authResponseMapper).toAuthResponseDTO(snapshot);
    }

    @Test
    @DisplayName("login → throws on invalid credentials")
    void login_invalid() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authValidator).validateCredentials(any(AuthRequestDTO.class));

        assertThatThrownBy(() -> authService.login(new AuthRequestDTO("john", "bad")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");

        verify(authValidator).validateCredentials(any(AuthRequestDTO.class));
        verifyNoInteractions(authResponseMapper);
    }

    @Test
    @DisplayName("register → throws when username already exists")
    void register_username_exists() {
        when(userAccountGateway.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new AuthRequestDTO("john", "secret")))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Username already exists");

        verify(authValidator).validateCredentials(any(AuthRequestDTO.class));
        verify(userAccountGateway).existsByUsername("john");
        verifyNoInteractions(authResponseMapper);
    }

    @Test
    @DisplayName("register → creates user and maps response via mapper")
    void register_ok() {
        when(userAccountGateway.existsByUsername("john")).thenReturn(false);
        when(userAccountGateway.createUser("john", "secret", Set.of(RoleName.USER)))
                .thenReturn(snapshot);
        when(authResponseMapper.toAuthResponseDTO(any(UserSnapshot.class)))
                .thenReturn(AuthResponseDTO.builder()
                        .username("john")
                        .token("jwt-token")
                        .roles(Set.of("USER"))
                        .scopes(Set.of("simulation:read"))
                        .build());

        var res = authService.register(new AuthRequestDTO("john", "secret"));

        assertThat(res).isNotNull();
        assertThat(res.getUsername()).isEqualTo("john");
        assertThat(res.getToken()).isEqualTo("jwt-token");
        assertThat(res.getRoles()).containsExactly("USER");
        assertThat(res.getScopes()).containsExactly("simulation:read");

        verify(authValidator).validateCredentials(any(AuthRequestDTO.class));
        verify(userAccountGateway).existsByUsername("john");
        verify(userAccountGateway).createUser("john", "secret", Set.of(RoleName.USER));
        verify(authResponseMapper).toAuthResponseDTO(snapshot);
    }
}

