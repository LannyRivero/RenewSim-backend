package com.renewsim.backend.simulation_service.history.application.query;

public record SimulationHistoryPageRequest(
        int page,
        int size,
        SortField sortField,
        SortDirection sortDirection) {

    public SimulationHistoryPageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (sortField == null || sortDirection == null) {
            throw new IllegalArgumentException("Sort field and direction are required");
        }
    }

    public enum SortField { CREATED_AT, NAME, TECHNOLOGY, LOCATION, STATUS }

    public enum SortDirection { ASC, DESC }
}
