package com.renewsim.backend.auth_service.infrastructure.policy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.renewsim.backend.role_service.domain.model.RoleName;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class YamlScopePolicyTest {

    @Test
    @DisplayName("scopesFor(USER) should return configured scopes")
    void testShouldReturnConfiguredScopes_ForKnownRole() {
        YamlScopePolicy policy = new YamlScopePolicy();
        Map<RoleName, Set<String>> map = new HashMap<>();
        map.put(RoleName.USER, Set.of("simulation:read", "profile:read"));
        map.put(RoleName.ADMIN, Set.of("admin:write"));
        policy.setRoleScopes(map);

        Set<String> scopes = policy.scopesFor(RoleName.USER);

        assertThat(scopes)
                .containsExactlyInAnyOrder("simulation:read", "profile:read");
    }

    @Test
    @DisplayName("scopesFor(unknown) should return empty set")
    void testShouldReturnEmptySet_ForUnknownRole() {
       YamlScopePolicy policy = new YamlScopePolicy();
        policy.setRoleScopes(Map.of(RoleName.USER, Set.of("simulation:read")));

        Set<String> scopes = policy.scopesFor(RoleName.ADMIN);

        assertThat(scopes).isEmpty();
    }

    @Test
    @DisplayName("scopesFor returns a defensive copy (internal map must not be mutated)")
    void testShouldReturnDefensiveCopy() {
        YamlScopePolicy policy = new YamlScopePolicy();
        policy.setRoleScopes(new HashMap<>(Map.of(
                RoleName.USER, new HashSet<>(Set.of("simulation:read"))
        )));

        Set<String> first = policy.scopesFor(RoleName.USER);
        first.add("hacked:scope"); 

        Set<String> second = policy.scopesFor(RoleName.USER);
        assertThat(second).containsExactly("simulation:read");
    }
}
