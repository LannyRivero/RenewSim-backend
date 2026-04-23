package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.application.command.ResendOtpCommand;
import com.renewsim.backend.auth_service.application.result.ResendOtpResult;

public interface ResendOtpUseCase {
    ResendOtpResult execute(ResendOtpCommand command);
}