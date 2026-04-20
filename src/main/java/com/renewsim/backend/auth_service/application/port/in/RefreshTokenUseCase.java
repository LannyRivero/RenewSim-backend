package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.RefreshTokenCommand;
import com.renewsim.backend.auth_service.application.result.RefreshTokenResultDTO;

public interface RefreshTokenUseCase {
    RefreshTokenResultDTO execute(RefreshTokenCommand command);
}