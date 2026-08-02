package com.renewsim.backend.simulation_service.shared.web;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import org.springframework.security.core.Authentication;

/**
 * Extracts the simulation request security context from Spring Authentication.
 */
public final class SimulationRequestContextFactory {

    public SimulationRequestContext from(Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return new SimulationRequestContext(user.username(), isAdmin);
    }
}
