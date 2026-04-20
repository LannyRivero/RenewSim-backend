package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.application.port.in.common.GetByIdUseCase;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import com.renewsim.backend.shared.exception.UserNotFoundException;

public interface GetUserUseCase extends GetByIdUseCase<UserResponse, Long> {

    UserResponse getUserById(Long id);

    @Override
    default UserResponse getById(Long id) {
        return getUserById(id);
    }

    UserResponse getUserByUsernameOrEmail(String username, String email);

    User getDomainUserByUsernameOrEmail(String username, String email);

    /**
     * Retrieves a domain user by ID, intended only for internal flows.
     *
     * @param id the user ID
     * @return the {@link User} domain object
     * @throws UserNotFoundException if no user exists with the given ID
     */
    User getDomainUserById(Long id);
}