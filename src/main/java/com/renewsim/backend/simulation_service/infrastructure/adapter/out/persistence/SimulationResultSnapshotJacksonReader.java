package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationResultSnapshotReaderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimulationResultSnapshotJacksonReader implements SimulationResultSnapshotReaderPort {

    private final ObjectMapper objectMapper;

    @Override
    public SimulationDetailsResult read(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, SimulationDetailsResult.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize simulation payload", ex);
        }
    }
}
