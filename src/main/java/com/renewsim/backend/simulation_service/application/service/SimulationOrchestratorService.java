package com.renewsim.backend.simulation_service.application.service;

import com.renewsim.backend.simulation_service.application.command.CreateSimulationCommand;
import com.renewsim.backend.simulation_service.web.dto.SimulationRecommendationResultDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SimulationOrchestratorService
 *
 * Orquesta la creación de simulaciones y la asignación automática de
 * tecnologías recomendadas.
 * Garantiza persistencia coherente y consistencia transaccional siguiendo
 * principios DDD.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SimulationOrchestratorService {

        private final SimulationApplicationService simulationApplicationService;

        /**
         * Crea una simulación y recomienda tecnologías según su tipo de energía.
         * Flujo:
         * 1 Validar y obtener datos climáticos.
         * 2 Crear y guardar simulación base.
         * 3 Generar recomendaciones de tecnologías.
         * 4 Asignarlas y persistir la simulación actualizada.
         */
        public SimulationRecommendationResultDTO createSimulationWithRecommendation(CreateSimulationCommand command) {
                return simulationApplicationService.createSimulationWithRecommendation(command);
        }

}
