package com.renewsim.backend.simulation_service.domain.policy;

import com.renewsim.backend.simulation_service.domain.model.SimulationRecommendation;

public final class SimulationRecommendationReviewPolicy {

    public boolean needsReview(SimulationRecommendation recommendation, boolean hasWarnings, boolean hasDetails) {
        if (!hasDetails) {
            return true;
        }
        if (recommendation != SimulationRecommendation.RECOMMENDED) {
            return true;
        }
        return hasWarnings;
    }

    public String nextStepFor(SimulationRecommendation recommendation) {
        return switch (recommendation) {
            case RECOMMENDED -> "Validar sensibilidad y elevar a evaluacion ejecutiva";
            case VIABLE_WITH_RESERVATIONS -> "Revisar supuestos criticos antes de priorizar inversion";
            case NOT_RECOMMENDED -> "Completar datos y replantear supuestos antes de avanzar";
        };
    }
}
