package com.renewsim.backend.simulation_service.domain.factory;

import java.util.List;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.*;

public final class SimulationFactory {

    private SimulationFactory() {}

    public static Simulation create(
        String name,
        String location,
        double latitude,
        double longitude,
        EnergyType energyType,
        double projectSize,
        double budget,
        ClimateData climateData,
        List<Long> technologyIds,
        String createdBy
    ) {
        return Simulation.create(
            name,
            location,
            latitude,
            longitude,
            energyType,
            new ProjectSize(projectSize),
            new Budget(budget),
            climateData,
            technologyIds,
            createdBy
        );
    }
}

