package com.renewsim.backend.simulation_service.domain.policy;

public final class SimulationPortfolioPriorityPolicy {

    public String priorityFor(int score, boolean hasDetails) {
        if (!hasDetails) {
            return "REVIEW";
        }
        if (score >= 75) {
            return "HIGH";
        }
        if (score >= 50) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
