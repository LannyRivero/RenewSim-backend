package com.renewsim.backend.simulation_service.application.historySimulation;

public interface ListUserRealSimulationsUseCase {
    UserSimulationListResult getUserSimulations(String username);
}
