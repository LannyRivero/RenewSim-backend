package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.LoginStep1Command;
import com.renewsim.backend.auth_service.application.result.LoginStep1ResultDTO;

public interface LoginStep1UseCase {
    LoginStep1ResultDTO execute(LoginStep1Command command);
}