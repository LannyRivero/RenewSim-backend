package com.renewsim.backend.simulation_service.shared.web;

public record SimulationRequestContext(
                String username,
                boolean isAdmin) {
}
