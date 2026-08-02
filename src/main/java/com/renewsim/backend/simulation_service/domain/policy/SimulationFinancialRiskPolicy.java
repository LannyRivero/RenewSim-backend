package com.renewsim.backend.simulation_service.domain.policy;

public final class SimulationFinancialRiskPolicy {

    private static final double LONG_PAYBACK_YEARS = 10.0;
    private static final double WEAK_ROI_PERCENT = 5.0;

    public String resolveMainRisk(String warningRisk, Double paybackYears, Double roiPercent) {
        if (warningRisk != null && !warningRisk.isBlank()) {
            return warningRisk;
        }
        if (paybackYears == null && roiPercent == null) {
            return "Informacion financiera incompleta";
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
}
