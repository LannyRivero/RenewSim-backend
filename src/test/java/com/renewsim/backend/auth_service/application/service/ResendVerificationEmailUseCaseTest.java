package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.EmailVerificationTokenRepository;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.service.ResendVerificationEmailUseCase.ResendVerificationException;
import com.renewsim.backend.auth_service.domain.model.EmailVerificationToken;
import com.renewsim.backend.shared.domain.vo.RoleName;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ResendVerificationEmailUseCase")
class ResendVerificationEmailUseCaseTest {

    private UserAccountGateway userAccountGateway;
    private EmailVerificationTokenRepository tokenRepository;
    private EmailPort emailPort;
    private ResendVerificationEmailUseCase useCase;

    private static final String EMAIL = "user@renewsim.com";

    private static final UserSnapshot UNVERIFIED_USER = UserSnapshot.disabled(
            1L, "user", "Test User", "hash", EMAIL, Set.of(RoleName.USER));

    private static final UserSnapshot VERIFIED_USER = UserSnapshot.active(
            1L, "user", "Test User", "hash", EMAIL, Set.of(RoleName.USER));

    @BeforeEach
    void setUp() {
        userAccountGateway = mock(UserAccountGateway.class);
        tokenRepository = mock(EmailVerificationTokenRepository.class);
        emailPort = mock(EmailPort.class);
        useCase = new ResendVerificationEmailUseCase(userAccountGateway, tokenRepository, emailPort);
    }

    // ─────────────────────────────────────────────
    // Happy path
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given an unverified user")
    class UnverifiedUser {

        @BeforeEach
        void setup() {
            when(userAccountGateway.findByEmail(EMAIL)).thenReturn(Optional.of(UNVERIFIED_USER));
        }

        @Test
        @DisplayName("should save new verification token")
        void shouldSaveNewToken() {
            useCase.execute(EMAIL);

            ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
            verify(tokenRepository).save(captor.capture());

            EmailVerificationToken saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(1L);
            assertThat(saved.getToken()).isNotNull().isNotBlank();
            assertThat(saved.getExpiresAt()).isAfter(java.time.LocalDateTime.now().minusMinutes(1));
        }

        @Test
        @DisplayName("should send verification email")
        void shouldSendVerificationEmail() {
            useCase.execute(EMAIL);

            verify(emailPort).sendVerificationEmail(
                    eq(EMAIL),
                    eq("Test User"),
                    anyString());
        }

        @Test
        @DisplayName("should generate unique tokens on each resend")
        void shouldGenerateUniqueTokens() {
            when(userAccountGateway.findByEmail(EMAIL)).thenReturn(Optional.of(UNVERIFIED_USER));

            useCase.execute(EMAIL);
            useCase.execute(EMAIL);

            ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
            verify(tokenRepository, times(2)).save(captor.capture());

            String token1 = captor.getAllValues().get(0).getToken();
            String token2 = captor.getAllValues().get(1).getToken();
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("should complete without exception")
        void shouldCompleteWithoutException() {
            assertThatNoException().isThrownBy(() -> useCase.execute(EMAIL));
        }

        @Test
        @DisplayName("token should be associated with correct userId")
        void tokenShouldBelongToCorrectUser() {
            useCase.execute(EMAIL);

            ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
            verify(tokenRepository).save(captor.capture());

            assertThat(captor.getValue().getUserId()).isEqualTo(UNVERIFIED_USER.id());
        }
    }

    // ─────────────────────────────────────────────
    // User not found
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given user does not exist")
    class UserNotFound {

        @BeforeEach
        void setup() {
            when(userAccountGateway.findByEmail(any())).thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("should throw ResendVerificationException")
        void shouldThrow() {
            assertThatThrownBy(() -> useCase.execute(EMAIL))
                    .isInstanceOf(ResendVerificationException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should never send email")
        void shouldNeverSendEmail() {
            assertThatThrownBy(() -> useCase.execute(EMAIL))
                    .isInstanceOf(ResendVerificationException.class);

            verifyNoInteractions(emailPort);
        }

        @Test
        @DisplayName("should never save token")
        void shouldNeverSaveToken() {
            assertThatThrownBy(() -> useCase.execute(EMAIL))
                    .isInstanceOf(ResendVerificationException.class);

            verifyNoInteractions(tokenRepository);
        }
    }

    // ─────────────────────────────────────────────
    // Already verified
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given user is already verified")
    class AlreadyVerified {

        @BeforeEach
        void setup() {
            when(userAccountGateway.findByEmail(EMAIL)).thenReturn(Optional.of(VERIFIED_USER));
        }

        @Test
        @DisplayName("should throw ResendVerificationException")
        void shouldThrow() {
            assertThatThrownBy(() -> useCase.execute(EMAIL))
                    .isInstanceOf(ResendVerificationException.class)
                    .hasMessageContaining("already verified");
        }

        @Test
        @DisplayName("should never send email")
        void shouldNeverSendEmail() {
            assertThatThrownBy(() -> useCase.execute(EMAIL))
                    .isInstanceOf(ResendVerificationException.class);

            verifyNoInteractions(emailPort);
        }

        @Test
        @DisplayName("should never save token")
        void shouldNeverSaveToken() {
            assertThatThrownBy(() -> useCase.execute(EMAIL))
                    .isInstanceOf(ResendVerificationException.class);

            verifyNoInteractions(tokenRepository);
        }
    }

    // ─────────────────────────────────────────────
    // Various emails
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given various unknown emails")
    class UnknownEmails {

        @ParameterizedTest(name = "email ''{0}'' should not be found")
        @ValueSource(strings = { "unknown@test.com", "ghost@renewsim.com", "nobody@mail.com" })
        @DisplayName("should throw for any unknown email")
        void shouldThrowForUnknownEmail(String email) {
            when(userAccountGateway.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(email))
                    .isInstanceOf(ResendVerificationException.class)
                    .hasMessageContaining("User not found");
        }
    }
}