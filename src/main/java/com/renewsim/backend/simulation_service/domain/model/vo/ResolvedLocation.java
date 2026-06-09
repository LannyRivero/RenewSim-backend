package com.renewsim.backend.simulation_service.domain.model.vo;

public record ResolvedLocation(
        String name,
        String country,
        double latitude,
        double longitude) {

    public ResolvedLocation(String name, String country) {
        this(name, country, 0.0, 0.0);
    }
}
