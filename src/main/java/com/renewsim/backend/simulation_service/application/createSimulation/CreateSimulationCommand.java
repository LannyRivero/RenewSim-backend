package com.renewsim.backend.simulation_service.application.createSimulation;

import java.util.List;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;

public record CreateSimulationCommand(
        String name,
        String location,
        double latitude,
        double longitude,
        EnergyType energyType,
        double projectSize,
        double budget,
        ClimateData climateData,
        List<Long> technologyIds,
        String createdBy) {
}
