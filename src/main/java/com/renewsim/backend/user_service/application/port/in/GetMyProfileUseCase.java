package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.web.dto.UserResponse;

public interface GetMyProfileUseCase {
    UserResponse getMyProfile(Long userId);
    UserResponse getMyProfileByEmail(String email);
}