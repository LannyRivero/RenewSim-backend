package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.domain.model.Simulation;

final class SimulationCommandMapper {

    private SimulationCommandMapper() {
    }

    static Simulation toNewSimulation(CreateRealSimulationCommand command) {
        return Simulation.create(
                command.name(),
                command.technology(),
                command.location(),
                command.system(),
                command.demand(),
                command.economics(),
                command.createdBy());
    }
}
