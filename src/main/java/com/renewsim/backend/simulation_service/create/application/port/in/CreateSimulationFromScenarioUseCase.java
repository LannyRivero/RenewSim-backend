package com.renewsim.backend.simulation_service.create.application.port.in;

import com.renewsim.backend.simulation_service.create.application.command.CreateSimulationFromScenarioCommand;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;

public interface CreateSimulationFromScenarioUseCase {

    SimulationDetailsResult createSimulationFromScenario(CreateSimulationFromScenarioCommand command);
}
