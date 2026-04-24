package com.renewsim.backend.auth_service.infrastructure.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.renewsim.backend.shared.domain.vo.RoleName;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YamlScopePolicyTest {

    @Test
    @DisplayName("scopesFor(USER) should return configured scopes")
    void testShouldReturnConfiguredScopes_ForKnownRole() {
        // Given
        YamlScopePolicy policy = new YamlScopePolicy();
        Map<RoleName, Set<String>> map = new HashMap<>();
        map.put(RoleName.USER, Set.of("simulation:read", "profile:read"));
        map.put(RoleName.ADMIN, Set.of("admin:write"));
        policy.setRoleScopes(map);

        // When
        Set<String> scopes = policy.scopesFor(RoleName.USER);

        // Then
        assertThat(scopes)
                .containsExactlyInAnyOrder("simulation:read", "profile:read");
    }

    @Test
    @DisplayName("scopesFor(unknown) should return empty set")
    void testShouldReturnEmptySet_ForUnknownRole() {
        // Given
        YamlScopePolicy policy = new YamlScopePolicy();
        policy.setRoleScopes(Map.of(RoleName.USER, Set.of("simulation:read")));

        // When
        Set<String> scopes = policy.scopesFor(RoleName.ADMIN);

        // Then
        assertThat(scopes).isEmpty();
    }

    @Test
    @DisplayName("scopesFor returns immutable set (cannot be modified)")
    void testShouldReturnDefensiveCopy() {
        // Given
        YamlScopePolicy policy = new YamlScopePolicy();
        Map<RoleName, Set<String>> map = new HashMap<>();
        map.put(RoleName.USER, new HashSet<>(Set.of("simulation:read")));
        policy.setRoleScopes(map);

        // When
        Set<String> scopes = policy.scopesFor(RoleName.USER);

        // Then - intentar mutar debe lanzar UnsupportedOperationException
        assertThatThrownBy(() -> scopes.add("hacked:scope"))
                .isInstanceOf(UnsupportedOperationException.class);

        // Verificar que el estado interno no cambió
        Set<String> second = policy.scopesFor(RoleName.USER);
        assertThat(second).containsExactly("simulation:read");
    }
}