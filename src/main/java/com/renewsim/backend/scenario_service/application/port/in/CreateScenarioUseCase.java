package com.renewsim.backend.scenario_service.application.port.in;

import com.renewsim.backend.scenario_service.application.command.CreateScenarioCommand;
import com.renewsim.backend.scenario_service.application.result.ScenarioResponseDTO;

public interface CreateScenarioUseCase {

    ScenarioResponseDTO createScenario(CreateScenarioCommand command);
}
