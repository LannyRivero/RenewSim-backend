package com.renewsim.backend.technology_service.application.port.in;

import com.renewsim.backend.technology_service.application.command.DeleteTechnologyCommand;

public interface DeleteTechnologyUseCase {
    void deleteTechnology(DeleteTechnologyCommand command);
}

