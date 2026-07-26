package com.renewsim.backend.simulation_service.domain.exception;

import com.renewsim.backend.simulation_service.domain.model.SimulationStatus;

public class InvalidSimulationStatusTransitionException extends RuntimeException {

    public InvalidSimulationStatusTransitionException(String action, SimulationStatus currentStatus) {
        super("Cannot " + action + " simulation in status: " + currentStatus);
    }
}
