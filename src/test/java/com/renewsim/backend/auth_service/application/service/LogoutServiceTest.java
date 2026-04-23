package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LogoutCommand;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenBlacklistPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LogoutResult;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutService")
class LogoutServiceTest {

        @Mock
        private TokenProvider tokenProvider;
        @Mock
        private TokenBlacklistPort tokenBlacklistPort;
        @Mock
        private RefreshTokenRepositoryPort refreshTokenRepositoryPort;
        @Mock
        private UserAccountGateway userAccountGateway;

        @InjectMocks
        private LogoutService service;

        private UserSnapshot activeUser;

        @BeforeEach
        void setUp() {
                activeUser = UserSnapshotMother.withEmail("john@example.com", Set.of(RoleName.USER));
        }

        @Test
        @DisplayName("valid token -> blacklists JTI and revokes refresh tokens")
        void execute_validToken_blacklistsAndRevokes() {
                when(tokenProvider.extractJti("valid-token")).thenReturn(Optional.of("jti-123"));
                when(tokenProvider.extractExpirationEpochSeconds("valid-token"))
                                .thenReturn(Optional.of(9999999L));
                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));

                LogoutResult result = service.execute(
                                new LogoutCommand("valid-token", "john@example.com"));

                assertThat(result.message()).isEqualTo("Logged out successfully");
                verify(tokenBlacklistPort).blacklist("jti-123", 1L, 9999999L);
                verify(refreshTokenRepositoryPort).revokeAllByUserId(1L);
        }

        @Test
        @DisplayName("token without JTI -> skips blacklist but still revokes refresh tokens")
        void execute_tokenWithoutJti_skipsBl­acklist() {
                when(tokenProvider.extractJti("no-jti-token")).thenReturn(Optional.empty());
                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));

                LogoutResult result = service.execute(
                                new LogoutCommand("no-jti-token", "john@example.com"));

                assertThat(result.message()).isEqualTo("Logged out successfully");
                verifyNoInteractions(tokenBlacklistPort);
                verify(refreshTokenRepositoryPort).revokeAllByUserId(1L);
        }

        @Test
        @DisplayName("unknown username -> skips blacklist and revocation silently")
        void execute_unknownUsername_skipsRevocation() {
                when(tokenProvider.extractJti("valid-token")).thenReturn(Optional.of("jti-456"));
                when(tokenProvider.extractExpirationEpochSeconds("valid-token"))
                                .thenReturn(Optional.of(9999999L));
                when(userAccountGateway.findByEmail("unknown@example.com"))
                                .thenReturn(Optional.empty());

                LogoutResult result = service.execute(
                                new LogoutCommand("valid-token", "unknown@example.com"));

                assertThat(result.message()).isEqualTo("Logged out successfully");
                verify(tokenBlacklistPort).blacklist("jti-456", null, 9999999L);
                verify(refreshTokenRepositoryPort, never()).revokeAllByUserId(any());
        }

        @Test
        @DisplayName("expiration not extractable -> blacklists with epoch 0")
        void execute_noExpiration_blacklistsWithZero() {
                when(tokenProvider.extractJti("valid-token")).thenReturn(Optional.of("jti-789"));
                when(tokenProvider.extractExpirationEpochSeconds("valid-token"))
                                .thenReturn(Optional.empty());
                when(userAccountGateway.findByEmail("john@example.com"))
                                .thenReturn(Optional.of(activeUser));

                service.execute(new LogoutCommand("valid-token", "john@example.com"));

                verify(tokenBlacklistPort).blacklist("jti-789", 1L, 0L);
        }

        @Test
        @DisplayName("always returns success message regardless of token state")
        void execute_alwaysReturnsSuccess() {
                when(tokenProvider.extractJti(anyString())).thenReturn(Optional.empty());
                when(userAccountGateway.findByEmail(anyString())).thenReturn(Optional.empty());

                LogoutResult result = service.execute(
                                new LogoutCommand("any-token", "any@example.com"));

                assertThat(result.message()).isEqualTo("Logged out successfully");
        }
}