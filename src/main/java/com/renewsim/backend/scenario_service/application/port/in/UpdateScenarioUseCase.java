package com.renewsim.backend.scenario_service.application.port.in;

import com.renewsim.backend.scenario_service.application.command.UpdateScenarioCommand;
import com.renewsim.backend.scenario_service.application.result.ScenarioResponseDTO;

public interface UpdateScenarioUseCase {

    ScenarioResponseDTO updateScenario(UpdateScenarioCommand command);
}
