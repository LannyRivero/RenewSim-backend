package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.LoginStep2Command;
import com.renewsim.backend.auth_service.application.result.LoginStep2Result;

public interface LoginStep2UseCase {
    LoginStep2Result execute(LoginStep2Command command);
}