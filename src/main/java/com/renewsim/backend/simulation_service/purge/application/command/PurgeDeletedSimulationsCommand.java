package com.renewsim.backend.simulation_service.purge.application.command;

public record PurgeDeletedSimulationsCommand(int retentionDays, String requestedBy) {

    public PurgeDeletedSimulationsCommand {
        if (retentionDays <= 0) {
            throw new IllegalArgumentException("retentionDays must be greater than zero");
        }
        if (requestedBy == null || requestedBy.isBlank()) {
            throw new IllegalArgumentException("requestedBy must not be blank");
        }
        requestedBy = requestedBy.trim();
    }
}
