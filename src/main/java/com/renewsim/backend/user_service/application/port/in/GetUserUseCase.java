package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserResponse;

/**
 * Use case for retrieving users from the system.
 *
 * <p>
 * Supports fetching users either by their unique ID or by their
 * username/email for authentication purposes.
 * </p>
 *
 * <p>
 * Security: Requires {@code ROLE_ADMIN} or {@code SCOPE_user:read}.
 * </p>
 */

public interface GetUserUseCase {

    /**
     * Retrieves a user by its unique identifier.
     *
     * @param id the user ID
     * @return the {@link UserResponse} representing the user
     * @throws UserNotFoundException if no user exists with the given ID
     */
    UserResponse getUserById(Long id);

    /**
     * Retrieves a domain user by username or email, intended for authentication
     * flows.
     *
     * @param username the username of the user, may be {@code null}
     * @param email    the email of the user, may be {@code null}
     * @return the {@link User} domain object
     * @throws UserNotFoundException if no user exists with the given criteria
     */
    UserResponse getUserByUsernameOrEmail(String username, String email);

    /**
     * Retrieves a domain user by username or email, intended mainly for internal
     * authentication flows.
     *
     * @param username the username of the user, may be {@code null}
     * @param email    the email of the user, may be {@code null}
     * @return the {@link User} domain object
     * @throws UserNotFoundException if no user exists with the given criteria
     */
    User getDomainUserByUsernameOrEmail(String username, String email);
}
