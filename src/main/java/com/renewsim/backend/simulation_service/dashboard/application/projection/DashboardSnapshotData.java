package com.renewsim.backend.simulation_service.dashboard.application.projection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DashboardSnapshotData(
        Summary summary,
        Technical technical,
        Financial financial,
        List<Warning> warnings) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Summary(
            String recommendation,
            String headline,
            List<Reason> reasons) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Reason(String message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Technical(double annualGenerationKwh) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Financial(
            double annualSavings,
            double netAnnualBenefit,
            Double paybackYears) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Warning(
            String severity,
            String message) {
    }
}
