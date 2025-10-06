package com.renewsim.backend.technology_service.application.port.in;

import com.renewsim.backend.technology_service.application.command.UpdateTechnologyCommand;
import com.renewsim.backend.technology_service.application.result.TechnologyUpdateResultDTO;

public interface UpdateTechnologyUseCase {
    TechnologyUpdateResultDTO updateTechnology(UpdateTechnologyCommand command);
}

