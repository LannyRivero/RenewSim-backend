package com.renewsim.backend.shared.security;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticatedRequestContextFactoryTest {

    private final AuthenticatedRequestContextFactory factory = new AuthenticatedRequestContextFactory();

    @Test
    @DisplayName("from extracts username and admin role from authentication")
    void fromExtractsUsernameAndAdminRoleFromAuthentication() {
        Authentication auth = authentication("alice", "ROLE_USER", "ROLE_ADMIN");

        AuthenticatedRequestContext context = factory.from(auth);

        assertThat(context.username()).isEqualTo("alice");
        assertThat(context.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("from marks non admin users correctly")
    void fromMarksNonAdminUsersCorrectly() {
        Authentication auth = authentication("bob", "ROLE_USER");

        AuthenticatedRequestContext context = factory.from(auth);

        assertThat(context.username()).isEqualTo("bob");
        assertThat(context.isAdmin()).isFalse();
    }

    private Authentication authentication(String username, String... roles) {
        AuthenticatedUser user = AuthenticatedUser.of(username, Set.of("USER"), Set.of("read:simulations"));
        return new TestingAuthenticationToken(
                user,
                null,
                List.of(roles).stream().map(SimpleGrantedAuthority::new).toList());
    }
}
