package com.renewsim.backend.simulation_service.application.deleteSimulation;

public interface DeleteAllSimulationsByUserUseCase {
    
    void deleteAllByUser(DeleteAllSimulationsByUserCommand command);

}
