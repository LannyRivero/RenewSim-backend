package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserResponse;

public interface GetUserUseCase {
    UserResponse getUserById(Long id);
    UserResponse getUserByUsernameOrEmail(String username, String email);
     User getDomainUserByUsernameOrEmail(String username, String email);
}

