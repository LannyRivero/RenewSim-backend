package com.renewsim.backend.simulation_service.location_lookup.web.dto;

public record ResolvedLocationResponseDTO(
        String name,
        String country,
        double lat,
        double lon) {
}
