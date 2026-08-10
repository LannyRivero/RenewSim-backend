package com.renewsim.backend.simulation_service.create.application.command;

import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;

public record CreateSimulationFromScenarioCommand(
                Long scenarioId,
                String name,
                SimulationLocation location,
                String createdBy) {
}
