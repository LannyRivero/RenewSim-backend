package com.renewsim.backend.auth_service.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.renewsim.backend.auth_service.application.command.ActivateAccountCommand;
import com.renewsim.backend.auth_service.application.port.out.ActivationTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.ActivateAccountResult;
import com.renewsim.backend.auth_service.domain.model.ActivationToken;
import com.renewsim.backend.auth_service.domain.service.TokenHasher;
import com.renewsim.backend.shared.exception.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class ActivateAccountServiceTest {

        @Mock
        private ActivationTokenRepositoryPort activationTokenRepositoryPort;

        @Mock
        private UserAccountGateway userAccountGateway;

        @Mock
        private TransactionalPort transactionalPort;

        @Mock
        private Clock clock;

        @InjectMocks
        private ActivateAccountService activateAccountService;

        private Clock fixedClock;

        @BeforeEach
        void setUp() {
                fixedClock = Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneOffset.UTC);
                lenient().when(clock.getZone()).thenReturn(ZoneOffset.UTC);
                lenient().when(clock.instant()).thenReturn(Instant.parse("2025-01-01T12:00:00Z"));

                when(transactionalPort.execute(any())).thenAnswer(inv -> {
                        java.util.function.Supplier<?> supplier = inv.getArgument(0);
                        return supplier.get();
                });
        }

        @Test
        @DisplayName("Should activate account when token is valid")
        void execute_whenTokenIsValid_shouldActivateAccountSuccessfully() {
                String rawToken = "plain-activation-token";
                String tokenHash = TokenHasher.hash(rawToken);
                LocalDateTime issuedAt = LocalDateTime.now(fixedClock).minusMinutes(5);
                LocalDateTime expiresAt = LocalDateTime.now(fixedClock).plusHours(23);

                ActivationToken token = ActivationToken.reconstitute(
                                1L,
                                10L,
                                tokenHash,
                                issuedAt,
                                expiresAt,
                                false);

                when(activationTokenRepositoryPort.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));

                ActivateAccountResult result = activateAccountService.execute(new ActivateAccountCommand(rawToken));

                assertEquals("Account activated successfully", result.message());
                verify(userAccountGateway).activateUser(10L);
                verify(activationTokenRepositoryPort).save(any(ActivationToken.class));
        }

        @Test
        @DisplayName("Should throw when activation token does not exist")
        void execute_whenTokenDoesNotExist_shouldThrowAuthenticationException() {
                String rawToken = "missing-token";
                String tokenHash = TokenHasher.hash(rawToken);

                when(activationTokenRepositoryPort.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

                assertThrows(AuthenticationException.class,
                                () -> activateAccountService.execute(new ActivateAccountCommand(rawToken)));

                verify(userAccountGateway, never()).activateUser(any());
                verify(activationTokenRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when activation token is expired")
        void execute_whenTokenIsExpired_shouldThrowAuthenticationException() {
                String rawToken = "expired-token";
                String tokenHash = TokenHasher.hash(rawToken);
                LocalDateTime issuedAt = LocalDateTime.now(fixedClock).minusDays(2);
                LocalDateTime expiresAt = LocalDateTime.now(fixedClock).minusMinutes(1);

                ActivationToken token = ActivationToken.reconstitute(
                                1L,
                                10L,
                                tokenHash,
                                issuedAt,
                                expiresAt,
                                false);

                when(activationTokenRepositoryPort.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));

                assertThrows(AuthenticationException.class,
                                () -> activateAccountService.execute(new ActivateAccountCommand(rawToken)));

                verify(userAccountGateway, never()).activateUser(any());
                verify(activationTokenRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when activation token is already used")
        void execute_whenTokenIsAlreadyUsed_shouldThrowAuthenticationException() {
                String rawToken = "used-token";
                String tokenHash = TokenHasher.hash(rawToken);
                LocalDateTime issuedAt = LocalDateTime.now(fixedClock).minusHours(1);
                LocalDateTime expiresAt = LocalDateTime.now(fixedClock).plusHours(23);

                ActivationToken token = ActivationToken.reconstitute(
                                1L,
                                10L,
                                tokenHash,
                                issuedAt,
                                expiresAt,
                                true);

                when(activationTokenRepositoryPort.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));

                assertThrows(AuthenticationException.class,
                                () -> activateAccountService.execute(new ActivateAccountCommand(rawToken)));

                verify(userAccountGateway, never()).activateUser(any());
                verify(activationTokenRepositoryPort, never()).save(any());
        }
}