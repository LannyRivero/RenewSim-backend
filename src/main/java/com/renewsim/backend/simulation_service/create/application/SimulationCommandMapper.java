package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.domain.model.Simulation;

import java.util.List;

final class SimulationCommandMapper {

    private SimulationCommandMapper() {
    }

    static Simulation toNewSimulation(CreateRealSimulationCommand command, List<Long> technologyIds) {
        return Simulation.create(
                command.name(),
                command.technology(),
                command.location(),
                command.system(),
                command.demand(),
                command.economics(),
                technologyIds,
                command.scenarioId(),
                command.createdBy());
    }
}
