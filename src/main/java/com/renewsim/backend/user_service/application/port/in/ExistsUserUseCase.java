package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.application.port.in.common.ExistsUseCase;

/**
 * Use case for checking if a user exists in the system.
 *
 * <p>
 * Supports verification by user ID, username, or email.
 * </p>
 *
 * <p>
 * Security: Public endpoint, no authentication required.
 * </p>
 */
public interface ExistsUserUseCase extends ExistsUseCase<Long> {

    /**
     * Checks if a user exists with the given username or email.
     *
     * @param username the username to check, may be {@code null}
     * @param email    the email to check, may be {@code null}
     * @return {@code true} if a user exists with the given criteria, otherwise {@code false}
     * @throws IllegalArgumentException if both username and email are {@code null}
     */
    boolean existsByUsernameOrEmail(String username, String email);
}

