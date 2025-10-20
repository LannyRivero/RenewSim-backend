package com.renewsim.backend.simulation_service.application.service;

import org.springframework.stereotype.Component;
import com.renewsim.backend.simulation_service.domain.model.Simulation;

@Component
public class SimulationCalculator {

    public double calculateEnergyOutput(Simulation simulation) {
        return simulation.projectSize().value() * 1200; // kWh/year (base estimation)
    }

    public double calculateCo2Reduction(Simulation simulation) {
        return simulation.energyOutput().kwhPerYear() * 0.0007; // tons/year
    }

    public double calculateROI(Simulation simulation) {
        return (simulation.energyOutput().kwhPerYear() * 0.15) / simulation.budget().value();
    }
}
