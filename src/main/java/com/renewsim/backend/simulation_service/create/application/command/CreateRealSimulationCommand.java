package com.renewsim.backend.simulation_service.create.application.command;

import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;

public record CreateRealSimulationCommand(
                String name,
                Technology technology,
                SimulationLocation location,
                SimulationSystem system,
                ConsumptionProfile demand,
                SimulationEconomics economics,
                String createdBy) {
}
