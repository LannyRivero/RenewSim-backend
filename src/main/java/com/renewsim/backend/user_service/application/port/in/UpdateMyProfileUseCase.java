package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.application.command.UpdateMyProfileCommand;
import com.renewsim.backend.user_service.web.dto.UserResponse;

public interface UpdateMyProfileUseCase {
    UserResponse updateMyProfile(UpdateMyProfileCommand command);
}