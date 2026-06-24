package com.renewsim.backend.simulation_service.application.historySimulation;


import java.util.List;

public interface GetUserSimulationHistoryUseCase {
    List<SimulationHistoryResultDTO> getUserHistory(String username);
}

