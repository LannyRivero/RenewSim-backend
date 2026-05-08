package com.renewsim.backend.simulation_service.application.service;

import com.renewsim.backend.simulation_service.application.command.CreateSimulationCommand;
import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.application.port.out.SimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.factory.SimulationFactory;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;
import com.renewsim.backend.simulation_service.web.dto.SimulationRecommendationResultDTO;
import com.renewsim.backend.technology_service.application.service.TechnologyRecommenderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SimulationOrchestratorService
 *
 * Orquesta la creación de simulaciones y la asignación automática de
 * tecnologías recomendadas.
 * Garantiza persistencia coherente y consistencia transaccional siguiendo
 * principios DDD.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SimulationOrchestratorService {

        private final SimulationRepositoryPort repository;
        private final SimulationValidator validator;
        private final SimulationCalculator calculator;
        private final TechnologyRecommenderService recommender;
        private final ClimateDataProviderPort climateProvider;

        /**
         * Crea una simulación y recomienda tecnologías según su tipo de energía.
         * Flujo:
         * 1 Validar y obtener datos climáticos.
         * 2 Crear y guardar simulación base.
         * 3 Generar recomendaciones de tecnologías.
         * 4 Asignarlas y persistir la simulación actualizada.
         */
        public SimulationRecommendationResultDTO createSimulationWithRecommendation(CreateSimulationCommand command) {

                validator.validateProjectSize(command.projectSize());
                validator.validateBudget(command.budget());

                ClimateData climateData = command.climateData() != null
                                ? command.climateData()
                                : climateProvider.fetchClimateData(command.location());

                // 1️ Crear simulación base
                Simulation base = SimulationFactory.create(
                                command.location(),
                                command.energyType(),
                                command.projectSize(),
                                command.budget(),
                                climateData,
                                List.of(),
                                command.createdBy());

                // 2️ Calcular resultados
                EnergyOutput energyOutput = calculator.calculateEnergyOutput(base);
                CO2Reduction co2Reduction = calculator.calculateCo2Reduction(energyOutput);

                Simulation completed = base.withCalculatedResults(energyOutput, co2Reduction);

                // 3️ Guardar simulación COMPLETA
                Simulation saved = repository.save(completed);

                // 4️ Recomendar tecnologías
                List<Long> recommendedIds = recommender.recommendFor(saved);

                Simulation finalSimulation = saved.assignTechnologies(recommendedIds);

                repository.save(finalSimulation);

                return new SimulationRecommendationResultDTO(
                                finalSimulation.id(),
                                finalSimulation.energyType().name(),
                                finalSimulation.technologyIds());
        }

}
