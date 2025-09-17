package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;

/**
 * Use case for creating a new user in the system.
 *
 * <p>
 * This interface defines the contract for handling the creation of users,
 * ensuring validation, business rules, and persistence are applied
 * before returning a response object.
 * </p>
 *
 * <p>
 * Security: Requires {@code ROLE_ADMIN} or {@code SCOPE_user:write}.
 * </p>
 */

public interface CreateUserUseCase {

    /**
     * Creates a new user with the given request data.
     *
     * @param request the user creation request containing username, email,
     *                password, and roles
     * @return a {@link UserResponse} containing the created user details
     * @throws UserAlreadyExistsException if a user with the same username or email
     *                                    already exists
     * @throws InvalidUserDataException   if the request data is invalid
     */
    UserResponse createUser(UserCreateRequest request);
}
