package com.renewsim.backend.simulation_service.web.dto;

import java.util.List;

public record ListUserSimulationsResponseDTO(
                List<SimulationHistoryRowDTO> items,
                long total) {
}
