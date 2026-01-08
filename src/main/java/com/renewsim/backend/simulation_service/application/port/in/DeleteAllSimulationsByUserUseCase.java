package com.renewsim.backend.simulation_service.application.port.in;

import com.renewsim.backend.simulation_service.application.command.DeleteAllSimulationsByUserCommand;

public interface DeleteAllSimulationsByUserUseCase {
    
    void deleteAllByUser(DeleteAllSimulationsByUserCommand command);

}
