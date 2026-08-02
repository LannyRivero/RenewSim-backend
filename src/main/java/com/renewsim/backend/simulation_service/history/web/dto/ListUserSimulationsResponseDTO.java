package com.renewsim.backend.simulation_service.history.web.dto;

import java.util.List;

public record ListUserSimulationsResponseDTO(
                List<SimulationHistoryRowDTO> items,
                long total) {
}
