package com.renewsim.backend.auth_service.application.mapper;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.ScopePolicy;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.result.AuthResult;
import com.renewsim.backend.auth_service.application.service.TokenTimeService;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.testutil.mothers.UserSnapshotMother;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthResponseMapperTest {

    private final TokenProvider tokenProvider = mock(TokenProvider.class);
    private final ScopePolicy scopePolicy = mock(ScopePolicy.class);
    private final TokenTimeService tokenTimeService = mock(TokenTimeService.class);

    private final AuthResponseMapper mapper = new AuthResponseMapper(tokenProvider, scopePolicy, tokenTimeService);

    @Test
    @DisplayName("Should build AuthResult correctly from UserSnapshot")
    void shouldBuildAuthResultCorrectly() {
        // Given
        UserSnapshot user = UserSnapshotMother.activeUser("john", Set.of(RoleName.USER));

        Instant fixedExpireAt = Instant.parse("2025-01-01T00:00:00Z");

        when(tokenTimeService.calculateExpiration()).thenReturn(fixedExpireAt);
        when(scopePolicy.getScopes(Set.of(RoleName.USER))).thenReturn(Set.of("sim:read"));
        when(tokenProvider.generate(any())).thenReturn("jwt-token");

        // When
        AuthResult result = mapper.toAuthResult(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresAt()).isEqualTo(fixedExpireAt);
        assertThat(result.username()).isEqualTo("john");
        assertThat(result.roles()).containsExactly("USER");
        assertThat(result.scopes()).containsExactly("sim:read");

        verify(tokenTimeService).calculateExpiration();
        verify(scopePolicy).getScopes(Set.of(RoleName.USER));
        verify(tokenProvider).generate(any());
    }
}