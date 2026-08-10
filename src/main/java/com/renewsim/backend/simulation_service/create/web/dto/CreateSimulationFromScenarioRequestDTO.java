package com.renewsim.backend.simulation_service.create.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSimulationFromScenarioRequestDTO(
        @NotNull Long scenarioId,
        @Size(min = 2, max = 120) String name,
        @NotNull @Valid CreateSolarSimulationRequestDTO.LocationDTO location) {
}
