package com.renewsim.backend.simulation_service.history.application;

public interface ListUserRealSimulationsUseCase {
    UserSimulationListResult getUserSimulations(String username);
}
