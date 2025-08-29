package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.dto.UserResponse;

public interface GetUserUseCase {
    UserResponse getById(long id);
}

