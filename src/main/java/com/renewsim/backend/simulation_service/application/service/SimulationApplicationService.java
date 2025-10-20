package com.renewsim.backend.simulation_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.renewsim.backend.simulation_service.application.port.in.*;
import com.renewsim.backend.simulation_service.application.port.out.*;
import com.renewsim.backend.simulation_service.application.command.*;
import com.renewsim.backend.simulation_service.application.result.*;
import com.renewsim.backend.simulation_service.domain.factory.SimulationFactory;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SimulationApplicationService implements
        CreateSimulationUseCase,
        UpdateSimulationUseCase,
        DeleteSimulationUseCase,
        GetSimulationUseCase {

    private final SimulationRepositoryPort repository;
    private final SimulationValidator validator;
    private final SimulationCalculator calculator;

    // --------------------------------------------------
    // CREATE
    // --------------------------------------------------
    @Override
    public SimulationCreationResultDTO createSimulation(CreateSimulationCommand command) {
        validator.validateProjectSize(command.projectSize());
        validator.validateBudget(command.budget());

        Simulation simulation = SimulationFactory.create(
                command.location(),
                command.energyType(),
                command.projectSize(),
                command.budget(),
                command.climateData(),
                command.technologyIds());

        Simulation saved = repository.save(simulation);

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

        // Rebuild simulation with new data
        Simulation updated = SimulationFactory.create(
                command.location(),
                command.energyType(),
                command.projectSize(),
                command.budget(),
                command.climateData(),
                command.technologyIds());

        Simulation saved = repository.save(updated);

        double newEnergy = calculator.calculateEnergyOutput(saved);
        double newCo2 = calculator.calculateCo2Reduction(saved);

        return new SimulationUpdateResultDTO(
                saved.id(),
                saved.location(),
                saved.energyType().name(),
                saved.projectSize().value(),
                saved.budget().value(),
                newEnergy,
                newCo2,
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
    public SimulationQueryResultDTO getSimulationById(GetSimulationByIdCommand command) {
        Simulation simulation = repository.findById(command.id())
                .orElseThrow(() -> new SimulationNotFoundException(command.id()));

        double estimatedEnergy = calculator.calculateEnergyOutput(simulation);
        double co2Reduction = calculator.calculateCo2Reduction(simulation);

        return new SimulationQueryResultDTO(
                simulation.id(),
                simulation.location(),
                simulation.energyType().name(),
                simulation.projectSize().value(),
                simulation.budget().value(),
                estimatedEnergy,
                co2Reduction,
                simulation.createdAt(),
                simulation.technologyIds().stream().map(String::valueOf).toList());
    }
}
