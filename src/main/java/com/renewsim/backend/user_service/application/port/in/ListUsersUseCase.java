package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import com.renewsim.backend.user_service.dto.UserResponse;

public interface ListUsersUseCase {
    PageResponse<UserResponse> listUsers(int page, int size, UserFilterRequest filters);

}

