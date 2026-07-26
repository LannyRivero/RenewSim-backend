package com.renewsim.backend.simulation_service.domain.model;

public enum SimulationStatus {
    DRAFT,
    COMPLETED,
    DELETED;

    public boolean isTerminal() {
        return this == DELETED;
    }
}
