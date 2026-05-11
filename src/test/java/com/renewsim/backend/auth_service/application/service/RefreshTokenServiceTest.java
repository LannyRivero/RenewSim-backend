package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.RefreshTokenCommand;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.RefreshTokenResult;
import com.renewsim.backend.auth_service.application.validator.UserAccountValidator;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.model.RefreshToken;
import com.renewsim.backend.auth_service.domain.service.TokenHasher;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.AuthenticationException;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("RefreshTokenService")
class RefreshTokenServiceTest {

    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private UserAccountGateway userAccountGateway;
    private TokenProvider tokenProvider;
    private TransactionalPort transactionalPort;
    private UserAccountValidator userAccountValidator;
    private Clock clock;
    private RefreshTokenService service;

    // Fixed clock for deterministic tests
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneId.of("UTC"));

    // A raw token and its hash
    private static final String RAW_TOKEN = "raw-refresh-token-value";
    private static final String TOKEN_HASH = TokenHasher.hash(RAW_TOKEN);

    private static final UserSnapshot ACTIVE_USER = UserSnapshot.active(
            1L, "user@renewsim.com", "Test User", "hash",
            "user@renewsim.com", Set.of(RoleName.USER));

    @BeforeEach
    void setUp() {
        refreshTokenRepositoryPort = mock(RefreshTokenRepositoryPort.class);
        userAccountGateway = mock(UserAccountGateway.class);
        tokenProvider = mock(TokenProvider.class);
        transactionalPort = mock(TransactionalPort.class);
        userAccountValidator = new UserAccountValidator();
        clock = FIXED_CLOCK;

        when(transactionalPort.execute(any())).thenAnswer(inv ->
                inv.getArgument(0, Supplier.class).get());

        service = new RefreshTokenService(
                refreshTokenRepositoryPort,
                userAccountGateway,
                tokenProvider,
                transactionalPort,
                clock,
                userAccountValidator);
    }

    // ─────────────────────────────────────────────
    // Happy path — valid token rotation
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given a valid refresh token")
    class ValidToken {

        private RefreshToken validToken;

        @BeforeEach
        void setup() {
            validToken = RefreshToken.issue(1L, TOKEN_HASH, clock, 7 * 24 * 3600L);
            when(refreshTokenRepositoryPort.findByTokenHash(TOKEN_HASH))
                    .thenReturn(Optional.of(validToken));
            when(userAccountGateway.findById(1L)).thenReturn(Optional.of(ACTIVE_USER));
            when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn("new-access-token");
            when(tokenProvider.expiresInSeconds()).thenReturn(3600L);
        }

        @Test
        @DisplayName("should return new access token")
        void shouldReturnNewAccessToken() {
            RefreshTokenResult result = service.execute(new RefreshTokenCommand(RAW_TOKEN));

            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.expiresIn()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("should return raw refresh token in result")
        void shouldReturnRawRefreshToken() {
            RefreshTokenResult result = service.execute(new RefreshTokenCommand(RAW_TOKEN));

            assertThat(result.rawRefreshToken()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("should revoke old token")
        void shouldRevokeOldToken() {
            service.execute(new RefreshTokenCommand(RAW_TOKEN));

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepositoryPort, times(2)).save(captor.capture());

            RefreshToken revokedToken = captor.getAllValues().get(0);
            assertThat(revokedToken.isRevoked()).isTrue();
            assertThat(revokedToken.getRevokedAt()).isNotNull();
        }

        @Test
        @DisplayName("should save new refresh token")
        void shouldSaveNewRefreshToken() {
            service.execute(new RefreshTokenCommand(RAW_TOKEN));

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepositoryPort, times(2)).save(captor.capture());

            RefreshToken newToken = captor.getAllValues().get(1);
            assertThat(newToken.isRevoked()).isFalse();
            assertThat(newToken.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should include user roles in result")
        void shouldIncludeRolesInResult() {
            RefreshTokenResult result = service.execute(new RefreshTokenCommand(RAW_TOKEN));

            assertThat(result.roles()).contains("USER");
            assertThat(result.username()).isEqualTo("user@renewsim.com");
        }

        @Test
        @DisplayName("new refresh token hash should differ from old one")
        void newTokenHashShouldDifferFromOld() {
            service.execute(new RefreshTokenCommand(RAW_TOKEN));

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepositoryPort, times(2)).save(captor.capture());

            String oldHash = captor.getAllValues().get(0).getTokenHash();
            String newHash = captor.getAllValues().get(1).getTokenHash();
            assertThat(newHash).isNotEqualTo(oldHash);
        }
    }

    // ─────────────────────────────────────────────
    // Token not found
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given token not found in repository")
    class TokenNotFound {

        @BeforeEach
        void setup() {
            when(refreshTokenRepositoryPort.findByTokenHash(any()))
                    .thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("should throw AuthenticationException")
        void shouldThrowAuthenticationException() {
            assertThatThrownBy(() -> service.execute(new RefreshTokenCommand(RAW_TOKEN)))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid or expired");
        }

        @Test
        @DisplayName("should never generate access token")
        void shouldNeverGenerateAccessToken() {
            assertThatThrownBy(() -> service.execute(new RefreshTokenCommand(RAW_TOKEN)))
                    .isInstanceOf(AuthenticationException.class);

            verify(tokenProvider, never()).generate(any());
        }
    }

    // ─────────────────────────────────────────────
    // Token expired or revoked
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given token is expired or revoked")
    class InvalidToken {

        @Test
        @DisplayName("should throw AuthenticationException when token is revoked")
        void shouldThrowWhenRevoked() {
            RefreshToken revokedToken = RefreshToken.issue(1L, TOKEN_HASH, clock, 7 * 24 * 3600L)
                    .revoked(clock);
            when(refreshTokenRepositoryPort.findByTokenHash(TOKEN_HASH))
                    .thenReturn(Optional.of(revokedToken));

            assertThatThrownBy(() -> service.execute(new RefreshTokenCommand(RAW_TOKEN)))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid or expired");
        }

        @Test
        @DisplayName("should throw AuthenticationException when token is expired")
        void shouldThrowWhenExpired() {
            // Issue token in the past — already expired
            Clock pastClock = Clock.fixed(
                    Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
            RefreshToken expiredToken = RefreshToken.issue(1L, TOKEN_HASH, pastClock, 1L);

            when(refreshTokenRepositoryPort.findByTokenHash(TOKEN_HASH))
                    .thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> service.execute(new RefreshTokenCommand(RAW_TOKEN)))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid or expired");
        }
    }

    // ─────────────────────────────────────────────
    // User not found after valid token
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given user not found after token lookup")
    class UserNotFound {

        @BeforeEach
        void setup() {
            RefreshToken validToken = RefreshToken.issue(1L, TOKEN_HASH, clock, 7 * 24 * 3600L);
            when(refreshTokenRepositoryPort.findByTokenHash(TOKEN_HASH))
                    .thenReturn(Optional.of(validToken));
            when(userAccountGateway.findById(1L)).thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("should throw AuthenticationException")
        void shouldThrow() {
            assertThatThrownBy(() -> service.execute(new RefreshTokenCommand(RAW_TOKEN)))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User not found");
        }
    }

    // ─────────────────────────────────────────────
    // User account disabled
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given user account is disabled")
    class DisabledUser {

        @BeforeEach
        void setup() {
            UserSnapshot disabledUser = UserSnapshot.disabled(
                    1L, "user@renewsim.com", "Test User", "hash",
                    "user@renewsim.com", Set.of(RoleName.USER));

            RefreshToken validToken = RefreshToken.issue(1L, TOKEN_HASH, clock, 7 * 24 * 3600L);
            when(refreshTokenRepositoryPort.findByTokenHash(TOKEN_HASH))
                    .thenReturn(Optional.of(validToken));
            when(userAccountGateway.findById(1L)).thenReturn(Optional.of(disabledUser));
        }

        @Test
        @DisplayName("should throw AuthenticationException")
        void shouldThrow() {
            assertThatThrownBy(() -> service.execute(new RefreshTokenCommand(RAW_TOKEN)))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("should never generate access token")
        void shouldNeverGenerateToken() {
            assertThatThrownBy(() -> service.execute(new RefreshTokenCommand(RAW_TOKEN)))
                    .isInstanceOf(AuthenticationException.class);

            verify(tokenProvider, never()).generate(any());
        }
    }
}