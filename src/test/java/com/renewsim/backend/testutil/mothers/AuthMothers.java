package com.renewsim.backend.testutil.mothers;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;

import java.util.Set;

/**
 * Test mother for authentication-related DTOs and domain objects.
 */
public final class AuthMothers {

    private AuthMothers() {
    }

    public static AuthenticatedUser authenticatedJohn() {
        return new AuthenticatedUser("john", Set.of("USER"), Set.of("sim:read"));
    }

    public static AuthRequestDTO loginRequestJohn() {
        return new AuthRequestDTO("john", "secret");
    }

    public static AuthRequestDTO loginRequestJane() {
        return new AuthRequestDTO("jane", "secret123");
    }
}

