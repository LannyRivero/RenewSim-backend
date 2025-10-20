package com.renewsim.backend.simulation_service.domain.factory;

import java.time.LocalDateTime;
import java.util.List;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.*;

public final class SimulationFactory {

    private SimulationFactory() {}

    public static Simulation create(
        String location,
        EnergyType energyType,
        double projectSize,
        double budget,
        ClimateData climateData,
        List<Long> technologyIds
    ) {
        return new Simulation(
            null,
            location,
            energyType,
            new ProjectSize(projectSize),
            new Budget(budget),
            new EnergyOutput(0), 
            new CO2Reduction(0), 
            climateData,
            technologyIds,
            LocalDateTime.now()
        );
    }
}

