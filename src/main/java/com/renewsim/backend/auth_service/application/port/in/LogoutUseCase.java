package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.LogoutCommand;
import com.renewsim.backend.auth_service.application.result.LogoutResult;

public interface LogoutUseCase {
    LogoutResult execute(LogoutCommand command);
}