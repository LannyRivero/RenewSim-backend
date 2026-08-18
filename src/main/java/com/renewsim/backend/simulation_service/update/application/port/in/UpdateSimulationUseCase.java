package com.renewsim.backend.simulation_service.update.application.port.in;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.update.application.command.UpdateSimulationCommand;

public interface UpdateSimulationUseCase {

    SimulationDetailsResult updateSimulation(UpdateSimulationCommand command, String requesterUsername,
            boolean isAdmin);
}
