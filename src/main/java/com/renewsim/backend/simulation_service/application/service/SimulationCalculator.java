package com.renewsim.backend.simulation_service.application.service;

import org.springframework.stereotype.Component;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;

@Component
public class SimulationCalculator {

    private static final double HOURS_PER_YEAR = 8760.0;
    private static final double DISCOUNT_RATE = 0.08;
    private static final int PROJECT_LIFETIME_YEARS = 25;
    private static final double ELECTRICITY_TARIFF = 0.12;

    public EnergyOutput calculateEnergyOutput(Simulation simulation) {
        double capacityFactor = calculateCapacityFactor(simulation);
        double value = simulation.projectSize().value() * HOURS_PER_YEAR * (capacityFactor / 100.0);
        return new EnergyOutput(value);
    }

    public CO2Reduction calculateCo2Reduction(EnergyOutput energyOutput) {
        return new CO2Reduction(energyOutput.kwhPerYear() * 0.0007);
    }

    public double calculateSavings(Simulation simulation) {
        return calculateAnnualRevenue(simulation);
    }

    public double calculateRoiYears(Simulation simulation) {
        double savings = calculateAnnualNetCashFlow(simulation);

        if (savings <= 0) {
                return -1;
        }

        return (int) Math.ceil(
                simulation.budget().value() / savings);
    }

    public double calculateCapacityFactor(Simulation simulation) {
        ClimateData climateData = simulation.climateData();
        if (climateData == null) {
            return baseCapacityFactor(simulation.energyType());
        }

        double modeledFactor = switch (simulation.energyType()) {
            case SOLAR -> (climateData.irradiance() / 1000.0) * 22.0;
            case WIND -> (climateData.wind() / 12.0) * 38.0;
            case HYDRO -> (climateData.hydrology() / 150.0) * 50.0;
            case HYBRID -> (((climateData.irradiance() / 1000.0) * 22.0) + ((climateData.wind() / 12.0) * 38.0)) / 2.0;
            case GEOTHERMAL -> 85.0;
            case BIOMASS -> 70.0;
        };

        return clamp(modeledFactor, 5.0, 95.0);
    }

    public double estimateCapex(EnergyType energyType, double installedCapacityKw) {
        return installedCapacityKw * switch (energyType) {
            case SOLAR -> 900.0;
            case WIND -> 1600.0;
            case HYDRO -> 2500.0;
            case HYBRID -> 1800.0;
            case GEOTHERMAL -> 3200.0;
            case BIOMASS -> 2100.0;
        };
    }

    public double estimateOpex(Simulation simulation) {
        double rate = switch (simulation.energyType()) {
            case SOLAR -> 0.02;
            case WIND -> 0.03;
            case HYDRO -> 0.04;
            case HYBRID -> 0.035;
            case GEOTHERMAL -> 0.045;
            case BIOMASS -> 0.05;
        };
        return simulation.budget().value() * rate;
    }

    public double calculateAnnualRevenue(Simulation simulation) {
        return simulation.energyOutput().kwhPerYear() * ELECTRICITY_TARIFF;
    }

    public double calculateAnnualNetCashFlow(Simulation simulation) {
        return calculateAnnualRevenue(simulation) - estimateOpex(simulation);
    }

    public double calculateRoiPercent(Simulation simulation) {
        return (calculateAnnualNetCashFlow(simulation) / simulation.budget().value()) * 100.0;
    }

    public double calculateNpv(Simulation simulation) {
        double annualNetCashFlow = calculateAnnualNetCashFlow(simulation);
        double discountedCashFlow = 0.0;
        for (int year = 1; year <= PROJECT_LIFETIME_YEARS; year++) {
            discountedCashFlow += annualNetCashFlow / Math.pow(1 + DISCOUNT_RATE, year);
        }
        return discountedCashFlow - simulation.budget().value();
    }

    public double calculateIrr(Simulation simulation) {
        double annualNetCashFlow = calculateAnnualNetCashFlow(simulation);
        double low = -0.99;
        double high = 1.0;

        for (int i = 0; i < 60; i++) {
            double mid = (low + high) / 2.0;
            double npv = -simulation.budget().value();
            for (int year = 1; year <= PROJECT_LIFETIME_YEARS; year++) {
                npv += annualNetCashFlow / Math.pow(1 + mid, year);
            }
            if (npv > 0) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return ((low + high) / 2.0) * 100.0;
    }

    private double baseCapacityFactor(EnergyType energyType) {
        return switch (energyType) {
            case SOLAR -> 20.0;
            case WIND -> 35.0;
            case HYDRO -> 45.0;
            case HYBRID -> 28.0;
            case GEOTHERMAL -> 85.0;
            case BIOMASS -> 70.0;
        };
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
