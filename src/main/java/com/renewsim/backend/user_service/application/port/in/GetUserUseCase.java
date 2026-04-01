package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.application.port.in.common.GetByIdUseCase;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import com.renewsim.backend.shared.exception.UserNotFoundException;

/**
 * Use case for retrieving users from the system.
 *
 * <p>
 * Supports fetching users by ID, username, or email.
 * Exposes both DTO-based responses for external layers and
 * domain objects for internal authentication.
 * </p>
 *
 * <p>
 * Security: Requires {@code ROLE_ADMIN} or {@code SCOPE_user:read}.
 * </p>
 */
public interface GetUserUseCase extends GetByIdUseCase<UserResponse, Long> {

    /**
     * Retrieves a user by its unique identifier.
     *
     * @param id the user ID
     * @return the {@link UserResponse} representing the user
     * @throws UserNotFoundException if no user exists with the given ID
     */
    UserResponse getUserById(Long id);

    @Override
    default UserResponse getById(Long id) {
        return getUserById(id);
    }

    /**
     * Retrieves a user by username or email, intended for external calls
     * such as admin panels or APIs.
     *
     * @param username the username of the user, may be {@code null}
     * @param email    the email of the user, may be {@code null}
     * @return the {@link UserResponse} DTO
     * @throws UserNotFoundException if no user exists with the given criteria
     */
    UserResponse getUserByUsernameOrEmail(String username, String email);

    /**
     * Retrieves a domain user by username or email, intended only for
     * internal authentication flows.
     *
     * @param username the username of the user, may be {@code null}
     * @param email    the email of the user, may be {@code null}
     * @return the {@link User} domain object
     * @throws UserNotFoundException if no user exists with the given criteria
     */
    User getDomainUserByUsernameOrEmail(String username, String email);
}
