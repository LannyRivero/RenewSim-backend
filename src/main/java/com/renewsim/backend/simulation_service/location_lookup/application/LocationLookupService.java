package com.renewsim.backend.simulation_service.location_lookup.application;

import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.location_lookup.application.port.in.LocationLookupUseCase;
import com.renewsim.backend.simulation_service.location_lookup.application.port.out.LocationLookupProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnBean(LocationLookupProvider.class)
@RequiredArgsConstructor
public class LocationLookupService implements LocationLookupUseCase {

    private final LocationLookupProvider locationLookupProvider;

    @Override
    public ResolvedLocation resolveLocation(double latitude, double longitude) {
        return locationLookupProvider.resolveLocation(latitude, longitude);
    }

    @Override
    public List<ResolvedLocation> searchLocations(String query, int limit) {
        return locationLookupProvider.searchLocations(query, limit);
    }
}
