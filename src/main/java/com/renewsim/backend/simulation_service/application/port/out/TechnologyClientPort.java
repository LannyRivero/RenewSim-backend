package com.renewsim.backend.simulation_service.application.port.out;

import com.renewsim.backend.simulation_service.web.dto.TechnologyResponseDTO;
import java.util.List;

/**
 * 🔌 TechnologyClientPort
 *
 * Output port that defines how the Simulation Service interacts
 * with the external Technology Service through Feign.
 */
public interface TechnologyClientPort {

    /**
     * Fetch all available technologies from the Technology Service.
     */
    List<TechnologyResponseDTO> getAllTechnologies();

    /**
     * Fetch detailed information about a specific technology by ID.
     */
    TechnologyResponseDTO getTechnologyById(Long id);
}
