package com.renewsim.backend.technology_service.application.port.in;

import com.renewsim.backend.technology_service.application.command.CreateTechnologyCommand;
import com.renewsim.backend.technology_service.application.result.TechnologyCreationResultDTO;

public interface CreateTechnologyUseCase {
    TechnologyCreationResultDTO createTechnology(CreateTechnologyCommand command);
}
