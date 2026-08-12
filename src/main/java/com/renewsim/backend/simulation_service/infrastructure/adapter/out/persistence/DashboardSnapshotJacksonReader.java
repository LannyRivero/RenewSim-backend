package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.dashboard.application.port.out.DashboardSnapshotReaderPort;
import com.renewsim.backend.simulation_service.dashboard.application.projection.DashboardSnapshotData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardSnapshotJacksonReader implements DashboardSnapshotReaderPort {

    private final ObjectMapper objectMapper;

    @Override
    public DashboardSnapshotData read(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, DashboardSnapshotData.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize dashboard snapshot payload", ex);
        }
    }
}
