package com.renewsim.backend.auth_service.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.renewsim.backend.auth_service.application.command.ActivateAccountCommand;
import com.renewsim.backend.auth_service.application.port.out.ActivationTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.ActivateAccountResultDTO;
import com.renewsim.backend.auth_service.domain.model.ActivationToken;
import com.renewsim.backend.auth_service.domain.service.TokenHasher;
import com.renewsim.backend.shared.exception.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class ActivateAccountServiceTest {

        @Mock
        private ActivationTokenRepositoryPort activationTokenRepositoryPort;

        @Mock
        private UserAccountGateway userAccountGateway;

        @InjectMocks
        private ActivateAccountService activateAccountService;

        @Test
        @DisplayName("Should activate account when token is valid")
        void execute_whenTokenIsValid_shouldActivateAccountSuccessfully() {
                String rawToken = "plain-activation-token";
                String tokenHash = TokenHasher.hash(rawToken);
                LocalDateTime issuedAt = LocalDateTime.now().minusMinutes(5);
                LocalDateTime expiresAt = LocalDateTime.now().plusHours(23);

                ActivationToken token = ActivationToken.reconstitute(
                                1L,
                                10L,
                                tokenHash,
                                issuedAt,
                                expiresAt,
                                false);

                when(activationTokenRepositoryPort.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));

                ActivateAccountResultDTO result = activateAccountService.execute(new ActivateAccountCommand(rawToken));

                assertEquals("Account activated successfully", result.message());
                assertTrue(token.isUsed());
                verify(userAccountGateway).activateUser(10L);
                verify(activationTokenRepositoryPort).save(token);
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
                LocalDateTime issuedAt = LocalDateTime.now().minusDays(2);
                LocalDateTime expiresAt = LocalDateTime.now().minusMinutes(1);

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
                LocalDateTime issuedAt = LocalDateTime.now().minusHours(1);
                LocalDateTime expiresAt = LocalDateTime.now().plusHours(23);

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