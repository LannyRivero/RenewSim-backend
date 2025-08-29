package com.renewsim.backend.auth_service.application;


import com.renewsim.backend.auth_service.application.port.out.*;
import com.renewsim.backend.auth_service.application.service.AuthServiceImpl;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.role.RoleName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceImplExpiresAtTest {

    @ParameterizedTest
    @ValueSource(longs = { 300L, 900L, 1800L, 3600L }) 
    void login_setsExpiresAt_usingClockAndExpiresIn(long ttlSeconds) {
        Instant base = Instant.parse("2025-01-01T00:00:00Z");
        Clock fixedClock = Clock.fixed(base, ZoneOffset.UTC);

        UserAccountGateway userGw = mock(UserAccountGateway.class);
        RoleProvider roleProvider = mock(RoleProvider.class);
        ScopePolicy scopePolicy = mock(ScopePolicy.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        TokenProvider tokenProvider = mock(TokenProvider.class);

        var service = new AuthServiceImpl(userGw, roleProvider, scopePolicy, encoder, tokenProvider, fixedClock);

        var snap = new UserSnapshot("john", "$2a$10$hash", Set.of(RoleName.USER));
        when(userGw.findByUsername("john")).thenReturn(Optional.of(snap));
        when(encoder.matches("secret", "$2a$10$hash")).thenReturn(true);
        when(scopePolicy.scopesFor(RoleName.USER)).thenReturn(Set.of("sim:read"));
        when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn("JWT");
        when(tokenProvider.expiresInSeconds()).thenReturn(ttlSeconds);

        var res = service.login(new AuthRequestDTO("john", "secret"));

        assertThat(res.getExpiresAt()).isEqualTo(base.plusSeconds(ttlSeconds));
        assertThat(res.getToken()).isEqualTo("JWT");
        verify(tokenProvider).expiresInSeconds();
        verify(tokenProvider).generate(any(AuthenticatedUser.class));
        verify(userGw).findByUsername("john");
        verify(encoder).matches("secret", "$2a$10$hash");
        verify(scopePolicy).scopesFor(RoleName.USER);
        Mockito.verifyNoMoreInteractions(tokenProvider, userGw, roleProvider);
    }
}

