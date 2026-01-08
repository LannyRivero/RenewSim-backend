package com.renewsim.backend.simulation_service.application.service;

import org.springframework.stereotype.Component;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;

@Component
public class SimulationCalculator {

    public EnergyOutput calculateEnergyOutput(Simulation simulation) {
        double value = simulation.projectSize().value() * 1200;
        return new EnergyOutput(value);
    }

    public CO2Reduction calculateCo2Reduction(EnergyOutput energyOutput) {
        return new CO2Reduction(energyOutput.kwhPerYear() * 0.0007);
    }

    public double calculateSavings(Simulation simulation) {
        double pricePerKwh = 0.25;
        return simulation.energyOutput().kwhPerYear() * pricePerKwh;
    }

    public int calculateRoiYears(Simulation simulation) {
        double savings = calculateSavings(simulation);

        if (savings <= 0) {
            return -1;
        }

        return (int) Math.ceil(
                simulation.budget().value() / savings);
    }
}
