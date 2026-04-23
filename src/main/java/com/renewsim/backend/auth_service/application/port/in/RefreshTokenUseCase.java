package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.RefreshTokenCommand;
import com.renewsim.backend.auth_service.application.result.RefreshTokenResult;

public interface RefreshTokenUseCase {
    RefreshTokenResult execute(RefreshTokenCommand command);
}