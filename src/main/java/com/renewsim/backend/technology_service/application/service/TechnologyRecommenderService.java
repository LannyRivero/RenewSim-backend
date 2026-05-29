package com.renewsim.backend.technology_service.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 *  Servicio de recomendación de tecnologías.
 * 
 * Evalúa el tipo de energía y devuelve una lista de IDs de tecnologías
 * recomendadas.
 * 
 *  Esta lógica puede evolucionar a un sistema más complejo basado en:
 * - Datos climáticos
 * - Coste de instalación
 * - Eficiencia esperada
 * - Machine Learning / Reglas de negocio configurables
 */
@Service
public class TechnologyRecommenderService {

    public List<Long> recommendForEnergyType(String energyType) {
        if (energyType == null || energyType.isBlank()) {
            return List.of();
        }

        String normalizedEnergyType = energyType.trim().toUpperCase();

        return switch (normalizedEnergyType) {
            case "SOLAR" -> List.of(1L);
            case "WIND" -> List.of(2L);
            case "HYDRO" -> List.of(3L);
            case "GEOTHERMAL" -> List.of(4L);
            case "BIOMASS" -> List.of(5L);
            default -> List.of();
        };
    }
}
