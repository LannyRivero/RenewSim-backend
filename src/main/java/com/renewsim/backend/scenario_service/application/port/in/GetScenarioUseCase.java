package com.renewsim.backend.scenario_service.application.port.in;

import com.renewsim.backend.scenario_service.application.command.GetScenarioByIdCommand;
import com.renewsim.backend.scenario_service.application.result.ScenarioResponseDTO;

import java.util.List;

public interface GetScenarioUseCase {

    List<ScenarioResponseDTO> getAllActiveScenarios();

    ScenarioResponseDTO getScenarioById(GetScenarioByIdCommand command);
}
