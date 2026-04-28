package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.AuthCommand;
import com.renewsim.backend.auth_service.application.command.RegisterCommand;
import com.renewsim.backend.auth_service.application.result.AuthResult;
import com.renewsim.backend.auth_service.application.result.RegisterResult;

public interface AuthUseCase {
    AuthResult login(AuthCommand command);

    RegisterResult register(RegisterCommand command);
}