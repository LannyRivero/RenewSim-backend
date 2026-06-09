package com.renewsim.backend.simulation_service.web.dto;

import java.time.LocalDateTime;

public record CreateSimulationResponseDTO(
    Long id,
    String name,
    String status,
    LocalDateTime createdAt
) {}
