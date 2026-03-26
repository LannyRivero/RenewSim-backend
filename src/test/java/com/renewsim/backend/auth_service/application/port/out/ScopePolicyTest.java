package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.shared.domain.vo.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ScopePolicyTest {

    private ScopePolicy scopePolicy;

    @BeforeEach
    void setUp() {
        scopePolicy = new ScopePolicy() {
            @Override
            public Set<String> scopesFor(RoleName roleName) {
                return switch (roleName) {
                    case ADMIN -> Set.of("admin:read", "admin:write");
                    case USER -> Set.of("user:read");
                    default -> Set.of();
                };
            }
        };
    }

    @Test
    void getScopes_shouldReturnCombinedScopesForMultipleRoles() {
        // Arrange
        Set<RoleName> roles = Set.of(RoleName.ADMIN, RoleName.USER);

        // Act
        Set<String> scopes = scopePolicy.getScopes(roles);

        // Assert
        assertEquals(Set.of("admin:read", "admin:write", "user:read"), scopes);
    }

    @Test
    void getScopes_shouldReturnEmptySetForNoRoles() {
        // Arrange
        Set<RoleName> roles = Set.of();

        // Act
        Set<String> scopes = scopePolicy.getScopes(roles);

        // Assert
        assertTrue(scopes.isEmpty());
    }

    @Test
    void getScopes_shouldReturnUnmodifiableSet() {
        // Arrange
        Set<RoleName> roles = Set.of(RoleName.ADMIN);

        // Act
        Set<String> scopes = scopePolicy.getScopes(roles);

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> scopes.add("new:scope"));
    }
}