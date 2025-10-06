package com.renewsim.backend.technology_service.application.result;

public record TechnologyDeletionResultDTO(
        Long id,
        boolean success,
        String message
) {}
