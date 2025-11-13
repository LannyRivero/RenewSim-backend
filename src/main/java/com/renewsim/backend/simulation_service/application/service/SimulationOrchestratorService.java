package com.renewsim.backend.simulation_service.application.service;

import com.renewsim.backend.simulation_service.application.command.CreateSimulationCommand;
import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.application.port.out.SimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.factory.SimulationFactory;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.dto.SimulationRecommendationResultDTO;
import com.renewsim.backend.technology_service.application.service.TechnologyRecommenderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 🎯 SimulationOrchestratorService
 *
 * Orquesta la creación de simulaciones y la asignación automática de tecnologías recomendadas.
 * Garantiza persistencia coherente y consistencia transaccional siguiendo principios DDD.
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
     *  1️⃣ Validar y obtener datos climáticos.
     *  2️⃣ Crear y guardar simulación base.
     *  3️⃣ Generar recomendaciones de tecnologías.
     *  4️⃣ Asignarlas y persistir la simulación actualizada.
     */
    public SimulationRecommendationResultDTO createSimulationWithRecommendation(CreateSimulationCommand command) {
        log.info("🚀 Creando simulación con recomendación | Tipo de energía: {}", command.energyType());

        // 1️⃣ Validación
        validator.validateProjectSize(command.projectSize());
        validator.validateBudget(command.budget());

        // 2️⃣ Obtener datos climáticos
        ClimateData climateData = command.climateData() != null
                ? command.climateData()
                : climateProvider.fetchClimateData(command.location());

        // 3️⃣ Crear simulación base
        Simulation base = SimulationFactory.create(
                command.location(),
                command.energyType(),
                command.projectSize(),
                command.budget(),
                climateData,
                List.of(),
                command.createdBy()
        );

        // 4️⃣ Guardar la simulación inicial para obtener su ID persistido
        Simulation savedBase = repository.save(base);
        log.info("💾 Simulación base guardada con ID: {}", savedBase.id());

        // 5️⃣ Obtener recomendaciones de tecnologías
        List<Long> recommendedIds = recommender.recommendFor(savedBase);
        log.info("✅ Tecnologías recomendadas para simulación {}: {}", savedBase.id(), recommendedIds);

        // 6️⃣ Asignar tecnologías y guardar nuevamente
        Simulation updated = savedBase.assignTechnologies(recommendedIds);
        Simulation finalSaved = repository.save(updated);

        log.info("💾 Simulación {} actualizada con tecnologías: {}", finalSaved.id(), finalSaved.technologyIds());

        // 7️⃣ Devolver DTO de resultado
        return new SimulationRecommendationResultDTO(
                finalSaved.id(),
                finalSaved.energyType().name(),
                finalSaved.technologyIds()
        );
    }
}
