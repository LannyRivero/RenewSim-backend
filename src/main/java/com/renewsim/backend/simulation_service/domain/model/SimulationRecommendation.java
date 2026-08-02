package com.renewsim.backend.simulation_service.domain.model;

public enum SimulationRecommendation {
    RECOMMENDED("recommended"),
    VIABLE_WITH_RESERVATIONS("viable_with_reservations"),
    NOT_RECOMMENDED("not_recommended");

    private final String wireValue;

    SimulationRecommendation(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static SimulationRecommendation fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            return NOT_RECOMMENDED;
        }
        for (SimulationRecommendation recommendation : values()) {
            if (recommendation.wireValue.equalsIgnoreCase(value.trim())) {
                return recommendation;
            }
        }
        return NOT_RECOMMENDED;
    }
}
