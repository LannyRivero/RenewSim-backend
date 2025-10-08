package com.renewsim.backend.technology_service.application.command;

import jakarta.validation.constraints.NotNull;

public record GetTechnologyByIdCommand(
        @NotNull(message = "Technology ID is required") Long id
) {}
