package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import com.renewsim.backend.simulation_service.application.port.out.TechnologyClientPort;
import com.renewsim.backend.simulation_service.web.dto.TechnologyResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 🧩 Adapter that implements the output port (TechnologyClientPort)
 * delegating REST calls to the Feign client.
 */
@Component
@RequiredArgsConstructor
public class TechnologyClientAdapter implements TechnologyClientPort {
    private final TechnologyClient client;

    @Override
    public List<TechnologyResponseDTO> getAllTechnologies() {
        return client.getAllTechnologies();
    }

    @Override
    public TechnologyResponseDTO getTechnologyById(Long id) {
        return client.getTechnologyById(id);
    }
}
