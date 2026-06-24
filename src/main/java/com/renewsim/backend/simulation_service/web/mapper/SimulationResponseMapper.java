package com.renewsim.backend.simulation_service.web.mapper;

import com.renewsim.backend.simulation_service.application.createSimulation.SimulationCreationResultDTO;
import com.renewsim.backend.simulation_service.application.detailSimulation.SimulationDetailResultDTO;
import com.renewsim.backend.simulation_service.application.historySimulation.SimulationHistoryResultDTO;
import com.renewsim.backend.simulation_service.application.service.SimulationCalculator;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Budget;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectSize;
import com.renewsim.backend.simulation_service.web.dto.CreateSimulationResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationClimateDataResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationFinancialsResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationLocationResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationLocationSummaryDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationResultsResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.UserSimulationSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class SimulationResponseMapper {

    private final SimulationCalculator calculator;

    public SimulationResponseMapper(SimulationCalculator calculator) {
        this.calculator = calculator;
    }

    public CreateSimulationResponseDTO toCreateResponse(SimulationCreationResultDTO result) {
        return new CreateSimulationResponseDTO(
                result.id(),
                result.name(),
                "completed",
                result.createdAt());
    }

    public SimulationResultsResponseDTO toResultsResponse(SimulationDetailResultDTO result) {
        Simulation simulation = toSimulation(result);
        double opex = calculator.estimateOpex(simulation);
        double revenue = calculator.calculateAnnualRevenue(simulation);
        double roi = calculator.calculateRoiPercent(simulation);
        double paybackYears = calculator.calculateRoiYears(simulation);
        double npv = calculator.calculateNpv(simulation);
        double irr = calculator.calculateIrr(simulation);

        return new SimulationResultsResponseDTO(
                result.id(),
                result.name(),
                result.createdAt(),
                new SimulationLocationResponseDTO(
                        result.location(),
                        resolveCountry(result.climateData()),
                        result.latitude(),
                        result.longitude()),
                new SimulationClimateDataResponseDTO(
                        result.climateData() != null ? result.climateData().irradiance() : 0.0,
                        result.climateData() != null ? result.climateData().wind() : 0.0,
                        result.climateData() != null ? result.climateData().hydrology() : 0.0,
                        result.climateData() != null ? result.climateData().temperature() : null,
                        result.climateData() != null ? result.climateData().source() : null,
                        result.climateData() != null ? result.climateData().period() : null),
                result.energyType().toLowerCase(),
                result.installedCapacity(),
                result.energyGenerated(),
                result.capacityFactor(),
                new SimulationFinancialsResponseDTO(
                        result.budget(),
                        opex,
                        revenue,
                        roi,
                        paybackYears < 0 ? null : paybackYears,
                        npv,
                        irr),
                null,
                null);
    }

    public UserSimulationSummaryDTO toUserSummary(SimulationHistoryResultDTO result) {
        return new UserSimulationSummaryDTO(
                result.id(),
                result.name(),
                result.energyType().toLowerCase(),
                result.installedCapacity(),
                result.energyGenerated(),
                result.roi(),
                result.status(),
                result.createdAt(),
                new SimulationLocationSummaryDTO(result.location(), firstNonBlank(result.country(), resolveCountry(null))));
    }

    private String resolveCountry(ClimateData climateData) {
        return climateData != null ? firstNonBlank(climateData.country(), "Unknown") : "Unknown";
    }

    private String firstNonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private Simulation toSimulation(SimulationDetailResultDTO result) {
        return new Simulation(
                result.id(),
                result.name(),
                result.location(),
                result.latitude(),
                result.longitude(),
                EnergyType.fromString(result.energyType()),
                new ProjectSize(result.installedCapacity()),
                new Budget(result.budget()),
                new EnergyOutput(result.energyGenerated()),
                new CO2Reduction(0),
                result.climateData(),
                result.technologyIds(),
                result.createdBy(),
                result.createdAt());
    }
}
