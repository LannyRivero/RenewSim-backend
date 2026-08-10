package com.renewsim.backend.simulation_service.create.web;

import com.renewsim.backend.simulation_service.create.application.command.CreateSimulationFromScenarioCommand;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSimulationFromScenarioRequestDTO;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;

public final class CreateSimulationFromScenarioWebMapper {

    public CreateSimulationFromScenarioCommand toCommand(
            CreateSimulationFromScenarioRequestDTO request, String username) {
        return new CreateSimulationFromScenarioCommand(
                request.scenarioId(),
                request.name(),
                SimulationLocation.of(
                        request.location().label(),
                        request.location().lat(),
                        request.location().lon(),
                        request.location().country(),
                        CountryCode.of(request.location().countryCode())),
                username);
    }
}
