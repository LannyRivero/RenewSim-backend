package com.renewsim.backend.simulation_service.location_lookup.application;

import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.location_lookup.application.port.in.LocationLookupUseCase;
import com.renewsim.backend.simulation_service.location_lookup.application.port.out.LocationLookupProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@ConditionalOnBean(LocationLookupProvider.class)
@RequiredArgsConstructor
public class LocationLookupService implements LocationLookupUseCase {

    private final LocationLookupProvider locationLookupProvider;

    @Override
    @Cacheable(
            value = "simulationLocationLookup",
            key = "'resolve:' + #root.target.coordinateKey(#latitude, #longitude)",
            unless = "#result == null || (#result.country() == 'Unknown' && #result.name() == #root.target.coordinateLabel(#latitude, #longitude))")
    public ResolvedLocation resolveLocation(double latitude, double longitude) {
        return locationLookupProvider.resolveLocation(latitude, longitude);
    }

    @Override
    @Cacheable(
            value = "simulationLocationLookup",
            key = "'search:' + #root.target.queryKey(#query) + ':' + #limit",
            unless = "#result == null || #result.isEmpty()")
    public List<ResolvedLocation> searchLocations(String query, int limit) {
        return locationLookupProvider.searchLocations(query, limit);
    }

    public String coordinateKey(double latitude, double longitude) {
        return String.format(Locale.ROOT, "%.4f:%.4f", latitude, longitude);
    }

    public String coordinateLabel(double latitude, double longitude) {
        return String.format(Locale.ROOT, "%.4f, %.4f", latitude, longitude);
    }

    public String queryKey(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }
}
