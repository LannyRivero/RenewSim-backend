package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;

public interface CreateUserUseCase {
    UserResponse create(UserCreateRequest request);
}

