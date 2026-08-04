package com.renewsim.backend.simulation_service.location_lookup.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;

import java.util.List;

public interface LocationLookupProvider {
    ResolvedLocation resolveLocation(double latitude, double longitude);

    List<ResolvedLocation> searchLocations(String query, int limit);
}
