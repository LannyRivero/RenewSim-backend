package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;
    
import com.renewsim.backend.simulation_service.application.port.out.TechnologyClientPort;
import com.renewsim.backend.simulation_service.domain.util.TechnologyScoringUtil.TechnologyData;
import com.renewsim.backend.simulation_service.dto.TechnologyResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter that implements the domain port (TechnologyClientPort)
 * using the Feign client (TechnologyClient) underneath.
 */
@Component
@RequiredArgsConstructor
public class TechnologyClientAdapter implements TechnologyClientPort {

    private final TechnologyClient technologyClient;

    @Override
    public List<TechnologyData> fetchTechnologiesForSimulation(Long simulationId) {
        List<TechnologyResponseDTO> responseList = technologyClient.getAllTechnologies();

        // Transform remote DTOs into domain TechnologyData
        return responseList.stream()
                .map(dto -> new TechnologyData(
                        dto.co2Reduction(),
                        dto.energyProduction(),
                        dto.installationCost(),
                        dto.efficiency()
                ))
                .toList();
    }
}
