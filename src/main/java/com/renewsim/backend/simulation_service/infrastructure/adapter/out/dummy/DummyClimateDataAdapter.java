package com.renewsim.backend.simulation_service.infrastructure.adapter.out.dummy;

import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "simulation.climate", name = "provider", havingValue = "dummy", matchIfMissing = true)
public class DummyClimateDataAdapter implements ClimateDataProviderPort {

    @Override
    public ClimateData fetchClimateData(double latitude, double longitude) {
        return new ClimateData(800, 5, 100, 22.0, "DUMMY", "static_fixture", null);
    }

    @Override
    public ResolvedLocation resolveLocation(double latitude, double longitude) {
        return new ResolvedLocation(String.format("%.4f, %.4f", latitude, longitude), "Unknown");
    }

    @Override
    public List<ResolvedLocation> searchLocations(String query, int limit) {
        return List.of();
    }
}

