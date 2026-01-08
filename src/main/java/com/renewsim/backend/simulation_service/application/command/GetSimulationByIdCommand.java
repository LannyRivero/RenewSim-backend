package com.renewsim.backend.simulation_service.application.command;

public record GetSimulationByIdCommand(
        Long id,
        String requesterUsername,
        boolean isAdmin) {
}
