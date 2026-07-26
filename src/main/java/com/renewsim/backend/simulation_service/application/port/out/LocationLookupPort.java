package com.renewsim.backend.simulation_service.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;

import java.util.List;

public interface LocationLookupPort {
    ResolvedLocation resolveLocation(double latitude, double longitude);

    List<ResolvedLocation> searchLocations(String query, int limit);
}
