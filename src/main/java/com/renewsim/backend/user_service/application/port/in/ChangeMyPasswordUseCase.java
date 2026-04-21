package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.application.command.ChangeMyPasswordCommand;

public interface ChangeMyPasswordUseCase {
    void changeMyPassword(ChangeMyPasswordCommand command);
}