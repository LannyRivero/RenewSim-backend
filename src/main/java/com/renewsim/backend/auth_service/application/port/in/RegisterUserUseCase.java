package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.RegisterCommand;
import com.renewsim.backend.auth_service.application.result.RegisterResult;

public interface RegisterUserUseCase {
    RegisterResult execute(RegisterCommand command);
}