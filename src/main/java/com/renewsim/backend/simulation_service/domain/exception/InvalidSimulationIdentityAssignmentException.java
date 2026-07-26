package com.renewsim.backend.simulation_service.domain.exception;

public class InvalidSimulationIdentityAssignmentException extends RuntimeException {

    public InvalidSimulationIdentityAssignmentException() {
        super("Simulation identity can only be assigned once");
    }
}
