package com.renewsim.backend.simulation_service.history.application.query;

import java.util.List;

public record SimulationHistoryPage<T>(
        List<T> content,
        long totalElements,
        int number,
        int size,
        int totalPages) {

    public SimulationHistoryPage {
        content = List.copyOf(content);
    }
}
