package com.renewsim.backend.simulation_service.application.command;

import java.util.List;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;

public record CreateSimulationCommand(
        String location,
        EnergyType energyType,
        double projectSize,
        double budget,
        ClimateData climateData,
        List<Long> technologyIds) {
}
