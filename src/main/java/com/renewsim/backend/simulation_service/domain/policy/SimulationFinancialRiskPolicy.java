package com.renewsim.backend.simulation_service.domain.policy;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;

import java.util.List;

public final class SimulationFinancialRiskPolicy {

    private static final double LONG_PAYBACK_YEARS = 10.0;
    private static final double WEAK_ROI_PERCENT = 5.0;

    public String resolveMainRisk(SimulationDetailsResult details, Double paybackYears, Double roiPercent) {
        if (details == null) {
            return "Informacion financiera incompleta";
        }

        String warningRisk = safeWarnings(details).stream()
                .filter(warning -> "warning".equalsIgnoreCase(warning.severity()))
                .map(SimulationDetailsResult.SimulationWarning::message)
                .findFirst()
                .orElse(null);
        if (warningRisk != null) {
            return warningRisk;
        }
        if (hasLongPayback(paybackYears)) {
            return "Payback por encima de la banda esperada";
        }
        if (hasWeakRoi(roiPercent)) {
            return "Retorno anual debil frente al CAPEX";
        }
        return "Sensibilidad moderada a supuestos economicos";
    }

    public boolean hasNegativeRoi(Double roiPercent) {
        return roiPercent != null && roiPercent < 0.0;
    }

    public boolean hasLongPayback(Double paybackYears) {
        return paybackYears != null && paybackYears > LONG_PAYBACK_YEARS;
    }

    public boolean hasWeakRoi(Double roiPercent) {
        return roiPercent != null && roiPercent < WEAK_ROI_PERCENT;
    }

    private List<SimulationDetailsResult.SimulationWarning> safeWarnings(SimulationDetailsResult details) {
        return details.warnings() == null ? List.of() : details.warnings();
    }
}
