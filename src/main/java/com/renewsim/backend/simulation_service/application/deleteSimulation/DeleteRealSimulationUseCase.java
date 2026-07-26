package com.renewsim.backend.simulation_service.application.deleteSimulation;

public interface DeleteRealSimulationUseCase {
    void deleteSimulation(Long id, String requesterUsername, boolean isAdmin);

    void deleteAllUserSimulations(String username);
}
