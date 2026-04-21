package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.LogoutCommand;
import com.renewsim.backend.auth_service.application.result.LogoutResultDTO;

public interface LogoutUseCase {
    LogoutResultDTO execute(LogoutCommand command);
}