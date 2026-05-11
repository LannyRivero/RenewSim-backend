package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.RegisterCommand;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.EmailVerificationTokenRepository;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.RegisterResult;
import com.renewsim.backend.auth_service.domain.model.AuthUserStatus;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.ConflictException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.core.env.Environment;
import java.util.function.Supplier;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RegisterUserService")
class RegisterUserServiceTest {

    private UserAccountGateway userAccountGateway;
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    private EmailPort emailPort;
    private TransactionalPort transactionalPort;
    private Environment environment;
    private RegisterUserService service;

    private static final RegisterCommand VALID_COMMAND = new RegisterCommand(
            "Test User", "SecurePass123!", "user@renewsim.com");

    private static final UserSnapshot INACTIVE_SNAPSHOT = UserSnapshot.disabled(
            1L, "user", "Test User", "hash", "user@renewsim.com", Set.of(RoleName.USER));

    private static final UserSnapshot ACTIVE_SNAPSHOT = UserSnapshot.active(
            1L, "user", "Test User", "hash", "user@renewsim.com", Set.of(RoleName.USER));

    @BeforeEach
    void setUp() {
        userAccountGateway = mock(UserAccountGateway.class);
        emailVerificationTokenRepository = mock(EmailVerificationTokenRepository.class);
        emailPort = mock(EmailPort.class);
        environment = mock(Environment.class);
        transactionalPort = mock(TransactionalPort.class);

        // TransactionalPort executes the supplier directly
        when(transactionalPort.execute(any())).thenAnswer(inv -> inv.getArgument(0, Supplier.class).get());

        service = new RegisterUserService(
                userAccountGateway,
                emailVerificationTokenRepository,
                emailPort,
                transactionalPort,
                environment,
                48);
    }

    // ─────────────────────────────────────────────
    // Happy path — non-local profile
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given non-local profile")
    class NonLocalProfile {

        @BeforeEach
        void nonLocal() {
            when(environment.getActiveProfiles()).thenReturn(new String[] { "prod" });
            when(userAccountGateway.existsByEmail(any())).thenReturn(false);
            when(userAccountGateway.createUser(any(), any(), any(), any(), any()))
                    .thenReturn(INACTIVE_SNAPSHOT);
        }

        @Test
        @DisplayName("should return INACTIVE result with verification message")
        void shouldReturnInactiveResult() {
            RegisterResult result = service.execute(VALID_COMMAND);

            assertThat(result.status()).isEqualTo(AuthUserStatus.INACTIVE);
            assertThat(result.email()).isEqualTo("user@renewsim.com");
            assertThat(result.message()).containsIgnoringCase("email");
        }

        @Test
        @DisplayName("should save email verification token")
        void shouldSaveVerificationToken() {
            service.execute(VALID_COMMAND);

            verify(emailVerificationTokenRepository).save(
                    argThat(token -> token.getUserId().equals(1L)
                            && token.getToken() != null
                            && !token.getToken().isBlank()));
        }

        @Test
        @DisplayName("should send verification email")
        void shouldSendVerificationEmail() {
            service.execute(VALID_COMMAND);

            verify(emailPort).sendVerificationEmail(
                    eq("user@renewsim.com"),
                    eq("Test User"),
                    anyString());
        }

        @Test
        @DisplayName("should never activate user")
        void shouldNeverActivateUser() {
            service.execute(VALID_COMMAND);

            verify(userAccountGateway, never()).activateUser(any());
        }

        @Test
        @DisplayName("should extract username from email")
        void shouldExtractUsernameFromEmail() {
            service.execute(VALID_COMMAND);

            verify(userAccountGateway).createUser(
                    eq("user"), any(), any(), any(), any());
        }

        @Test
        @DisplayName("should generate unique tokens on each registration")
        void shouldGenerateUniqueTokens() {
            when(userAccountGateway.createUser(any(), any(), any(), any(), any()))
                    .thenReturn(INACTIVE_SNAPSHOT);

            ArgumentCaptor<com.renewsim.backend.auth_service.domain.model.EmailVerificationToken> captor = ArgumentCaptor
                    .forClass(
                            com.renewsim.backend.auth_service.domain.model.EmailVerificationToken.class);

            service.execute(VALID_COMMAND);
            service.execute(new RegisterCommand("Other User", "Pass123!", "other@renewsim.com"));

            verify(emailVerificationTokenRepository, times(2)).save(captor.capture());
            var tokens = captor.getAllValues();
            assertThat(tokens.get(0).getToken()).isNotEqualTo(tokens.get(1).getToken());
        }

        @Test
        @DisplayName("should assign default USER role on creation")
        void shouldAssignDefaultUserRole() {
            service.execute(VALID_COMMAND);

            verify(userAccountGateway).createUser(
                    any(), any(), any(), any(),
                    argThat(roles -> roles.contains(RoleName.USER) && roles.size() == 1));
        }
    }

    // ─────────────────────────────────────────────
    // Happy path — local profile
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given local profile")
    class LocalProfile {

        @BeforeEach
        void localProfile() {
            when(environment.getActiveProfiles()).thenReturn(new String[] { "local" });
            when(userAccountGateway.existsByEmail(any())).thenReturn(false);
            when(userAccountGateway.createUser(any(), any(), any(), any(), any()))
                    .thenReturn(ACTIVE_SNAPSHOT);
        }

        @Test
        @DisplayName("should return ACTIVE result immediately")
        void shouldReturnActiveResult() {
            RegisterResult result = service.execute(VALID_COMMAND);

            assertThat(result.status()).isEqualTo(AuthUserStatus.ACTIVE);
            assertThat(result.message()).containsIgnoringCase("local");
        }

        @Test
        @DisplayName("should auto-activate user")
        void shouldAutoActivateUser() {
            service.execute(VALID_COMMAND);

            verify(userAccountGateway).activateUser(1L);
        }

        @Test
        @DisplayName("should not send verification email")
        void shouldNotSendVerificationEmail() {
            service.execute(VALID_COMMAND);

            verifyNoInteractions(emailPort);
        }

        @Test
        @DisplayName("should not save verification token")
        void shouldNotSaveVerificationToken() {
            service.execute(VALID_COMMAND);

            verifyNoInteractions(emailVerificationTokenRepository);
        }
    }

    // ─────────────────────────────────────────────
    // Conflict — email already registered
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Given email already exists")
    class EmailConflict {

        @BeforeEach
        void emailExists() {
            when(userAccountGateway.existsByEmail(any())).thenReturn(true);
        }

        @Test
        @DisplayName("should throw ConflictException")
        void shouldThrowConflictException() {
            assertThatThrownBy(() -> service.execute(VALID_COMMAND))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already registered");
        }

        @Test
        @DisplayName("should never create user")
        void shouldNeverCreateUser() {
            assertThatThrownBy(() -> service.execute(VALID_COMMAND))
                    .isInstanceOf(ConflictException.class);

            verify(userAccountGateway, never()).createUser(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("should never send email")
        void shouldNeverSendEmail() {
            assertThatThrownBy(() -> service.execute(VALID_COMMAND))
                    .isInstanceOf(ConflictException.class);

            verifyNoInteractions(emailPort);
        }
    }

    // ─────────────────────────────────────────────
    // Profile detection
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Profile detection")
    class ProfileDetection {

        @ParameterizedTest(name = "profile ''{0}'' should NOT auto-activate")
        @ValueSource(strings = { "prod", "docker", "staging", "test" })
        @DisplayName("non-local profiles should require email verification")
        void nonLocalProfilesShouldRequireVerification(String profile) {
            when(environment.getActiveProfiles()).thenReturn(new String[] { profile });
            when(userAccountGateway.existsByEmail(any())).thenReturn(false);
            when(userAccountGateway.createUser(any(), any(), any(), any(), any()))
                    .thenReturn(INACTIVE_SNAPSHOT);

            RegisterResult result = service.execute(VALID_COMMAND);

            assertThat(result.status()).isEqualTo(AuthUserStatus.INACTIVE);
            verify(userAccountGateway, never()).activateUser(any());
        }

        @Test
        @DisplayName("multiple active profiles including local should auto-activate")
        void multipleProfilesWithLocalShouldAutoActivate() {
            when(environment.getActiveProfiles()).thenReturn(new String[] { "local", "debug" });
            when(userAccountGateway.existsByEmail(any())).thenReturn(false);
            when(userAccountGateway.createUser(any(), any(), any(), any(), any()))
                    .thenReturn(ACTIVE_SNAPSHOT);

            RegisterResult result = service.execute(VALID_COMMAND);

            assertThat(result.status()).isEqualTo(AuthUserStatus.ACTIVE);
            verify(userAccountGateway).activateUser(1L);
        }
    }

    // ─────────────────────────────────────────────
    // Token expiration
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("Token expiration")
    class TokenExpiration {

        @Test
        @DisplayName("token should expire after configured hours")
        void tokenShouldExpireAfterConfiguredHours() {
            when(environment.getActiveProfiles()).thenReturn(new String[] { "prod" });
            when(userAccountGateway.existsByEmail(any())).thenReturn(false);
            when(userAccountGateway.createUser(any(), any(), any(), any(), any()))
                    .thenReturn(INACTIVE_SNAPSHOT);

            ArgumentCaptor<com.renewsim.backend.auth_service.domain.model.EmailVerificationToken> captor = ArgumentCaptor
                    .forClass(
                            com.renewsim.backend.auth_service.domain.model.EmailVerificationToken.class);

            service.execute(VALID_COMMAND);

            verify(emailVerificationTokenRepository).save(captor.capture());
            var token = captor.getValue();

            assertThat(token.getExpiresAt())
                    .isAfter(java.time.LocalDateTime.now().plusHours(47))
                    .isBefore(java.time.LocalDateTime.now().plusHours(49));
        }
    }
}