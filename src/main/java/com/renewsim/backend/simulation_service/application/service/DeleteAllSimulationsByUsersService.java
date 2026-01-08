package com.renewsim.backend.simulation_service.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.simulation_service.application.command.DeleteAllSimulationsByUserCommand;
import com.renewsim.backend.simulation_service.application.port.in.DeleteAllSimulationsByUserUseCase;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRepositoryAdapter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteAllSimulationsByUsersService implements DeleteAllSimulationsByUserUseCase {

    private final SimulationRepositoryAdapter repository;

    @Override
    public void deleteAllByUser(DeleteAllSimulationsByUserCommand command) {
        repository.deleteAllByCreatedBy(command.username());
    }
}
