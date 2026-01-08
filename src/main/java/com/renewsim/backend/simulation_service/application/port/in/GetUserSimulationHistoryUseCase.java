package com.renewsim.backend.simulation_service.application.port.in;


import java.util.List;
import com.renewsim.backend.simulation_service.application.result.SimulationHistoryResultDTO;

public interface GetUserSimulationHistoryUseCase {
    List<SimulationHistoryResultDTO> getUserHistory(String username);
}

