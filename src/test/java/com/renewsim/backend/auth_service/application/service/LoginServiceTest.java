package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginCommand;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LoginResult;
import com.renewsim.backend.auth_service.application.validator.CredentialsValidator;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.model.RefreshToken;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService")
class LoginServiceTest {

    @Mock
    private UserAccountGateway userAccountGateway;
    @Mock
    private CredentialsValidator credentialsValidator;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock
    private TransactionalPort transactionalPort;
    @Mock
    private TokenTimeService tokenTimeService;

    private static final Instant FIXED_INSTANT = Instant.parse("2024-06-01T10:00:00Z");
    private final Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private LoginService loginService;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@renewsim.com";
    private static final String PASSWORD = "SecurePass123!";
    private static final String PASSWORD_HASH = "$2a$10$hashedvalue";
    private static final String USERNAME = "testuser";
    private static final String ACCESS_TOKEN = "access.jwt.token";
    private static final String REFRESH_TOKEN = "refresh.jwt.token";
    private static final long EXPIRES_IN = 3600L;

    private static final Set<RoleName> ROLES = Set.of(RoleName.USER);

    @BeforeEach
    void setUp() {
        loginService = new LoginService(
                userAccountGateway,
                credentialsValidator,
                tokenProvider,
                refreshTokenRepository,
                transactionalPort,
                tokenTimeService,
                clock);

        lenient().when(transactionalPort.execute(any()))
                .thenAnswer(inv -> inv.<java.util.function.Supplier<?>>getArgument(0).get());

        // TTL del refresh token — evita expiresAt == issuedAt con default 0L
        lenient().when(tokenProvider.refreshExpiresInSeconds()).thenReturn(604800L);
    }

    @Nested
    @DisplayName("Given valid credentials and active account")
    class HappyPath {

        @Test
        @DisplayName("should return LoginResult with access and refresh tokens")
        void execute_validCredentials_returnsLoginResult() {
            LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
            UserSnapshot activeUser = UserSnapshot.active(
                    USER_ID, USERNAME, "Full Name", PASSWORD_HASH, EMAIL, ROLES);

            when(userAccountGateway.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser));
            when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn(ACCESS_TOKEN);
            when(tokenProvider.generate(any(AuthenticatedUser.class), anyLong())).thenReturn(REFRESH_TOKEN);
            when(tokenTimeService.getAccessTokenValiditySeconds()).thenReturn(EXPIRES_IN);

            LoginResult result = loginService.execute(command);

            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.expiresIn()).isEqualTo(EXPIRES_IN);
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.username()).isEqualTo(USERNAME);
            assertThat(result.roles()).containsExactlyInAnyOrder("USER");
        }

        @Test
        @DisplayName("should persist the refresh token")
        void execute_successfulLogin_savesRefreshToken() {
            LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
            UserSnapshot activeUser = UserSnapshot.active(
                    USER_ID, USERNAME, "Full Name", PASSWORD_HASH, EMAIL, ROLES);

            when(userAccountGateway.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser));
            when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn(ACCESS_TOKEN);
            when(tokenProvider.generate(any(AuthenticatedUser.class), anyLong())).thenReturn(REFRESH_TOKEN);
            when(tokenTimeService.getAccessTokenValiditySeconds()).thenReturn(EXPIRES_IN);

            loginService.execute(command);

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());

            RefreshToken saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(USER_ID);
            assertThat(saved.getTokenHash()).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("should invoke transactionalPort to wrap the operation")
        void execute_alwaysWrapsInTransaction() {
            LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
            UserSnapshot activeUser = UserSnapshot.active(
                    USER_ID, USERNAME, "Full Name", PASSWORD_HASH, EMAIL, ROLES);

            when(userAccountGateway.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser));
            when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn(ACCESS_TOKEN);
            when(tokenProvider.generate(any(AuthenticatedUser.class), anyLong())).thenReturn(REFRESH_TOKEN);
            when(tokenTimeService.getAccessTokenValiditySeconds()).thenReturn(EXPIRES_IN);

            loginService.execute(command);

            verify(transactionalPort).execute(any());
        }
    }

    @Nested
    @DisplayName("Given user not found by email")
    class UserNotFound {

        @Test
        @DisplayName("should throw UnauthorizedException with AUTH_INVALID_CREDENTIALS")
        void execute_userNotFound_throwsUnauthorized() {
            LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
            when(userAccountGateway.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loginService.execute(command))
                    .isInstanceOf(UnauthorizedException.class);

            verifyNoInteractions(tokenProvider);
            verifyNoInteractions(refreshTokenRepository);
        }
    }

    @Nested
    @DisplayName("Given wrong password")
    class WrongPassword {

        @Test
        @DisplayName("should throw UnauthorizedException when password does not match")
        void execute_wrongPassword_throwsUnauthorized() {
            LoginCommand command = new LoginCommand(EMAIL, "WrongPassword!");
            UserSnapshot activeUser = UserSnapshot.active(
                    USER_ID, USERNAME, "Full Name", PASSWORD_HASH, EMAIL, ROLES);

            when(userAccountGateway.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser));
            doThrow(new UnauthorizedException("Invalid credentials"))
                    .when(credentialsValidator).validatePassword(anyString(), anyString());

            assertThatThrownBy(() -> loginService.execute(command))
                    .isInstanceOf(UnauthorizedException.class);

            verifyNoInteractions(tokenProvider);
            verifyNoInteractions(refreshTokenRepository);
        }
    }

    @Nested
    @DisplayName("Given disabled account")
    class AccountDisabled {

        @Test
        @DisplayName("should throw UnauthorizedException with AUTH_USER_DISABLED")
        void execute_accountDisabled_throwsUnauthorized() {
            LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
            UserSnapshot disabledUser = UserSnapshot.disabled(
                    USER_ID, USERNAME, "Full Name", PASSWORD_HASH, EMAIL, ROLES);

            when(userAccountGateway.findByEmail(EMAIL)).thenReturn(Optional.of(disabledUser));

            assertThatThrownBy(() -> loginService.execute(command))
                    .isInstanceOf(UnauthorizedException.class);

            verifyNoInteractions(tokenProvider);
            verifyNoInteractions(refreshTokenRepository);
        }
    }

    @Nested
    @DisplayName("Given user with multiple roles")
    class RoleMapping {

        @Test
        @DisplayName("should map all role names to LoginResult")
        void execute_multipleRoles_allMappedToResult() {
            Set<RoleName> multiRoles = Set.of(RoleName.USER, RoleName.ADMIN);
            LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
            UserSnapshot adminUser = UserSnapshot.active(
                    USER_ID, USERNAME, "Full Name", PASSWORD_HASH, EMAIL, multiRoles);

            when(userAccountGateway.findByEmail(EMAIL)).thenReturn(Optional.of(adminUser));
            when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn(ACCESS_TOKEN);
            when(tokenProvider.generate(any(AuthenticatedUser.class), anyLong())).thenReturn(REFRESH_TOKEN);
            when(tokenTimeService.getAccessTokenValiditySeconds()).thenReturn(EXPIRES_IN);

            LoginResult result = loginService.execute(command);

            assertThat(result.roles()).containsExactlyInAnyOrder("USER", "ADMIN");
        }
    }
}