package com.renewsim.backend.simulation_service.history.application.result;

import java.util.List;

public record UserSimulationListResult(List<SimulationHistoryRowResult> items, long total) {
}
