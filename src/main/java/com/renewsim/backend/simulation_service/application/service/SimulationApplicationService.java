package com.renewsim.backend.simulation_service.application.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.simulation_service.application.port.in.*;
import com.renewsim.backend.simulation_service.application.port.out.*;
import com.renewsim.backend.simulation_service.application.command.*;
import com.renewsim.backend.simulation_service.application.result.*;
import com.renewsim.backend.simulation_service.domain.factory.SimulationFactory;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.domain.model.vo.Budget;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectSize;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SimulationApplicationService implements
                CreateSimulationUseCase,
                UpdateSimulationUseCase,
                DeleteSimulationUseCase,
                GetSimulationUseCase,
                GetUserSimulationHistoryUseCase {

        private final SimulationRepositoryPort repository;
        private final SimulationValidator validator;
        private final SimulationCalculator calculator;
        private final ClimateDataProviderPort climateProvider;

        // --------------------------------------------------
        // CREATE
        // --------------------------------------------------
        @Override
        public SimulationCreationResultDTO createSimulation(CreateSimulationCommand command) {

                validator.validateProjectSize(command.projectSize());
                validator.validateBudget(command.budget());

                ClimateData climateData = command.climateData() != null
                                ? command.climateData()
                                : climateProvider.fetchClimateData(command.location());

                Simulation base = SimulationFactory.create(
                                command.location(),
                                command.energyType(),
                                command.projectSize(),
                                command.budget(),
                                climateData,
                                command.technologyIds(),
                                command.createdBy());

                EnergyOutput energyOutput = calculator.calculateEnergyOutput(base);

                CO2Reduction co2Reduction = calculator.calculateCo2Reduction(energyOutput);

                Simulation completed = base.withCalculatedResults(energyOutput, co2Reduction);
                Simulation saved = repository.save(completed);

                return new SimulationCreationResultDTO(
                                saved.id(),
                                saved.location(),
                                saved.energyType().name(),
                                saved.projectSize().value(),
                                saved.budget().value(),
                                saved.createdAt());
        }

        // --------------------------------------------------
        // UPDATE
        // --------------------------------------------------
        @Override
        public SimulationUpdateResultDTO updateSimulation(UpdateSimulationCommand command) {

                Simulation existing = repository.findById(command.id())
                                .orElseThrow(() -> new SimulationNotFoundException(command.id()));

                validator.validateProjectSize(command.projectSize());
                validator.validateBudget(command.budget());

                ClimateData climateData = command.climateData() != null
                                ? command.climateData()
                                : existing.climateData();

                // Se conserva identidad del aggregate
                Simulation updated = new Simulation(
                                existing.id(),
                                command.location(),
                                command.energyType(),
                                new ProjectSize(command.projectSize()),
                                new Budget(command.budget()),
                                existing.energyOutput(),
                                existing.co2Reduction(),
                                climateData,
                                command.technologyIds(),
                                existing.createdBy(),
                                existing.createdAt());

                EnergyOutput energy = calculator.calculateEnergyOutput(updated);
                CO2Reduction co2 = calculator.calculateCo2Reduction(energy);

                Simulation completed = updated.withCalculatedResults(energy, co2);
                Simulation saved = repository.save(completed);

                return new SimulationUpdateResultDTO(
                                saved.id(),
                                saved.location(),
                                saved.energyType().name(),
                                saved.projectSize().value(),
                                saved.budget().value(),
                                energy.kwhPerYear(),
                                co2.tonsPerYear(),
                                LocalDateTime.now());
        }

        // --------------------------------------------------
        // DELETE
        // --------------------------------------------------
        @Override
        public SimulationDeletionResultDTO deleteSimulation(DeleteSimulationCommand command) {

                Simulation existing = repository.findById(command.id())
                                .orElseThrow(() -> new SimulationNotFoundException(command.id()));

                repository.deleteById(existing.id());

                return new SimulationDeletionResultDTO(
                                existing.id(),
                                true,
                                "Simulation deleted successfully");
        }

        // --------------------------------------------------
        // GET BY ID
        // --------------------------------------------------
        @Override
        @Transactional(readOnly = true)
        public SimulationDetailResultDTO getSimulationById(GetSimulationByIdCommand command) {

                Simulation simulation = repository.findById(command.id())
                                .orElseThrow(() -> new SimulationNotFoundException(command.id()));

                if (!simulation.createdBy().equals(command.requesterUsername())
                                && !command.isAdmin()) {
                        throw new AccessDeniedException("Not owner of simulation");
                }

                double savings = calculator.calculateSavings(simulation);
                Double roiYears = calculator.calculateRoiYears(simulation);
                if (roiYears < 0)
                        roiYears = null;

                return new SimulationDetailResultDTO(
                                simulation.id(),
                                simulation.location(),
                                simulation.energyType().name(),
                                simulation.projectSize().value(),
                                simulation.budget().value(),
                                simulation.energyOutput().kwhPerYear(),
                                savings,
                                roiYears,
                                simulation.createdAt(),
                                simulation.createdBy()

                );
        }

        @Override
        @Transactional(readOnly = true)
        public List<SimulationHistoryResultDTO> getUserHistory(String username) {

                return repository.findAllByCreatedBy(username)
                                .stream()
                                .map(simulation -> {

                                        // Recalcular derivados
                                        EnergyOutput energy = simulation.energyOutput() != null
                                                        ? simulation.energyOutput()
                                                        : calculator.calculateEnergyOutput(simulation);

                                        double savings = calculator.calculateSavings(
                                                        simulation.withCalculatedResults(
                                                                        energy,
                                                                        calculator.calculateCo2Reduction(energy)));

                                        double roiYears = calculator.calculateRoiYears(
                                                        simulation.withCalculatedResults(
                                                                        energy,
                                                                        calculator.calculateCo2Reduction(energy)));

                                        return new SimulationHistoryResultDTO(
                                                        simulation.id(),
                                                        simulation.location(),
                                                        simulation.energyType().name(),
                                                        energy.kwhPerYear(),
                                                        savings,
                                                        roiYears >= 0 ? roiYears : null,
                                                        simulation.createdAt());
                                })
                                .toList();
        }

}
