package com.renewsim.backend.auth_service.application.mapper;

import com.renewsim.backend.auth_service.application.port.out.ScopePolicy;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.domain.TokenTimeService;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;
import com.renewsim.backend.testutil.mothers.UserSnapshotMother;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthResponseMapperTest {

    private final TokenProvider tokenProvider = mock(TokenProvider.class);
    private final ScopePolicy scopePolicy = mock(ScopePolicy.class);
    private final TokenTimeService tokenTimeService = mock(TokenTimeService.class);

    private final AuthResponseMapper mapper = new AuthResponseMapper(tokenProvider, scopePolicy, tokenTimeService);

    @Test
    @DisplayName("Should build AuthResponseDTO correctly from UserSnapshot")
    void shouldBuildAuthResponseCorrectly() {
        // Given
        UserSnapshot user = UserSnapshotMother.activeUser("john", Set.of(RoleName.USER));

        Instant fixedExpireAt = Instant.parse("2025-01-01T00:00:00Z");

        when(tokenTimeService.calculateExpiration()).thenReturn(fixedExpireAt);
        when(scopePolicy.getScopes(Set.of(RoleName.USER))).thenReturn(Set.of("sim:read"));
        when(tokenProvider.generate(any())).thenReturn("jwt-token");

        // When
        AuthResponseDTO response = mapper.toAuthResponseDTO(user);

        // Then
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(fixedExpireAt, response.getExpiresAt());
        assertEquals("john", response.getUsername());
        assertTrue(response.getRoles().contains("USER"));
        assertTrue(response.getScopes().contains("sim:read"));

        verify(tokenTimeService).calculateExpiration();
        verify(scopePolicy).getScopes(Set.of(RoleName.USER));
        verify(tokenProvider).generate(any());
    }

}
