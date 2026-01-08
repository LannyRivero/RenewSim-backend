package com.renewsim.backend.technology_service.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.renewsim.backend.simulation_service.domain.model.Simulation;

/**
 * 💡 Servicio de recomendación de tecnologías.
 * 
 * Evalúa el tipo de energía y devuelve una lista de IDs de tecnologías
 * recomendadas.
 * 
 * 🔸 Esta lógica puede evolucionar a un sistema más complejo basado en:
 * - Datos climáticos
 * - Coste de instalación
 * - Eficiencia esperada
 * - Machine Learning / Reglas de negocio configurables
 */
@Service
public class TechnologyRecommenderService {

    public List<Long> recommendFor(Simulation simulation) {
        if (simulation == null || simulation.energyType() == null) {
            return List.of();
        }

        return switch (simulation.energyType()) {
            case SOLAR -> List.of(1L); // Ejemplo: tecnología 1 = Panel solar
            case WIND -> List.of(2L); // Ejemplo: tecnología 2 = Turbina eólica
            case HYDRO -> List.of(3L); // Ejemplo: tecnología 3 = Microhidráulica
            case GEOTHERMAL -> List.of(4L);
            case BIOMASS -> List.of(5L);
            default -> List.of();
        };
    }
}
