package com.renewsim.backend.simulation_service.delete.application.port.in;

public interface DeleteRealSimulationUseCase {
    void deleteSimulation(Long id, String requesterUsername, boolean isAdmin);

    void deleteAllUserSimulations(String username);
}
