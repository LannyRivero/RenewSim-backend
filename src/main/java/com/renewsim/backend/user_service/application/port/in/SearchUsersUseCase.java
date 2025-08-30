package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserResponse;

public interface SearchUsersUseCase {
    PageResponse<UserResponse> searchUsers(int page, int size, String username, String email, Boolean enabled);
}

