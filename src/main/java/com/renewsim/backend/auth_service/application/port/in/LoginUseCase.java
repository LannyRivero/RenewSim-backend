package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.LoginCommand;
import com.renewsim.backend.auth_service.application.result.LoginResult;

/**
 * Use case for user authentication.
 */
public interface LoginUseCase {
    
    /**
     * Authenticates a user with email and password.
     *
     * @param command login credentials
     * @return JWT tokens and user info
     * @throws com.renewsim.backend.shared.exception.UnauthorizedException if credentials invalid or email not verified
     */
    LoginResult execute(LoginCommand command);
}