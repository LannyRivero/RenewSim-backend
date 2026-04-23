package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.ActivateAccountCommand;
import com.renewsim.backend.auth_service.application.result.ActivateAccountResult;

public interface ActivateAccountUseCase {
    ActivateAccountResult execute(ActivateAccountCommand command);
}