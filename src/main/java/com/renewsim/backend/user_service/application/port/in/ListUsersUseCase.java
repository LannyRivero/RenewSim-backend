package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserResponse;

public interface ListUsersUseCase {
    PageResponse<UserResponse> list(int page, int size, String username, String email, Boolean enabled);
}

