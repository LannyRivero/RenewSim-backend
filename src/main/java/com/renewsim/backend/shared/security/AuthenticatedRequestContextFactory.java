package com.renewsim.backend.shared.security;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedRequestContextFactory {

    public AuthenticatedRequestContext from(Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return new AuthenticatedRequestContext(user.username(), isAdmin);
    }
}
