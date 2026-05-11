package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.port.out.EmailVerificationTokenRepository;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.service.VerifyEmailUseCase.EmailVerificationException;
import com.renewsim.backend.auth_service.domain.model.EmailVerificationToken;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("VerifyEmailUseCase")
class VerifyEmailUseCaseTest {

    private EmailVerificationTokenRepository tokenRepository;
    private UserAccountGateway userAccountGateway;
    private VerifyEmailUseCase useCase;

    private static final String VALID_TOKEN = "valid-token-abc123";
    private static final Long USER_ID = 42L;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(EmailVerificationTokenRepository.class);
        userAccountGateway = mock(UserAccountGateway.class);
        useCase = new VerifyEmailUseCase(tokenRepository, userAccountGateway);
    }

    // ─────────────────────────────────────────────
    // Happy path
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given a valid unused non-expired token")
    class ValidToken {

        private EmailVerificationToken token;

        @BeforeEach
        void setup() {
            token = new EmailVerificationToken(
                    USER_ID, VALID_TOKEN, LocalDateTime.now().plusHours(24));
            when(tokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(token));
        }

        @Test
        @DisplayName("should activate user via gateway")
        void shouldActivateUser() {
            useCase.execute(VALID_TOKEN);

            verify(userAccountGateway).activateUser(USER_ID);
        }

        @Test
        @DisplayName("should mark token as verified")
        void shouldMarkTokenAsVerified() {
            useCase.execute(VALID_TOKEN);

            assertThat(token.isVerified()).isTrue();
            assertThat(token.getVerifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("should save verified token")
        void shouldSaveVerifiedToken() {
            useCase.execute(VALID_TOKEN);

            verify(tokenRepository).save(token);
        }

        @Test
        @DisplayName("should complete without exception")
        void shouldCompleteWithoutException() {
            assertThatNoException().isThrownBy(() -> useCase.execute(VALID_TOKEN));
        }
    }

    // ─────────────────────────────────────────────
    // Token not found
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given token does not exist")
    class TokenNotFound {

        @BeforeEach
        void setup() {
            when(tokenRepository.findByToken(any())).thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("should throw EmailVerificationException")
        void shouldThrow() {
            assertThatThrownBy(() -> useCase.execute("non-existent-token"))
                    .isInstanceOf(EmailVerificationException.class)
                    .hasMessageContaining("Invalid or expired");
        }

        @Test
        @DisplayName("should never activate user")
        void shouldNeverActivateUser() {
            assertThatThrownBy(() -> useCase.execute("non-existent-token"))
                    .isInstanceOf(EmailVerificationException.class);

            verify(userAccountGateway, never()).activateUser(any());
        }
    }

    // ─────────────────────────────────────────────
    // Token already used
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given token already verified")
    class AlreadyVerified {

        @BeforeEach
        void setup() {
            EmailVerificationToken usedToken = new EmailVerificationToken(
                    USER_ID, VALID_TOKEN, LocalDateTime.now().plusHours(24));
            usedToken.markAsVerified();
            when(tokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(usedToken));
        }

        @Test
        @DisplayName("should throw EmailVerificationException")
        void shouldThrow() {
            assertThatThrownBy(() -> useCase.execute(VALID_TOKEN))
                    .isInstanceOf(EmailVerificationException.class)
                    .hasMessageContaining("already been used");
        }

        @Test
        @DisplayName("should never activate user")
        void shouldNeverActivateUser() {
            assertThatThrownBy(() -> useCase.execute(VALID_TOKEN))
                    .isInstanceOf(EmailVerificationException.class);

            verify(userAccountGateway, never()).activateUser(any());
        }
    }

    // ─────────────────────────────────────────────
    // Token expired
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given token is expired")
    class ExpiredToken {

        @BeforeEach
        void setup() {
            EmailVerificationToken expiredToken = new EmailVerificationToken(
                    USER_ID, VALID_TOKEN, LocalDateTime.now().minusSeconds(1));
            when(tokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(expiredToken));
        }

        @Test
        @DisplayName("should throw EmailVerificationException")
        void shouldThrow() {
            assertThatThrownBy(() -> useCase.execute(VALID_TOKEN))
                    .isInstanceOf(EmailVerificationException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("should never activate user")
        void shouldNeverActivateUser() {
            assertThatThrownBy(() -> useCase.execute(VALID_TOKEN))
                    .isInstanceOf(EmailVerificationException.class);

            verify(userAccountGateway, never()).activateUser(any());
        }
    }

    // ─────────────────────────────────────────────
    // Token format variation
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given various invalid token strings")
    class InvalidTokenStrings {

        @ParameterizedTest(name = "token ''{0}'' should not be found")
        @ValueSource(strings = { "", " ", "wrong-token", "abc" })
        @DisplayName("should throw for unrecognized tokens")
        void shouldThrowForUnrecognizedTokens(String token) {
            when(tokenRepository.findByToken(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(token))
                    .isInstanceOf(EmailVerificationException.class);
        }
    }
}