package com.renewsim.backend.simulation_service.shared.web.dto;

import java.util.List;

public record SimulationInputSnapshotDTO(
                String name,
                String technology,
                LocationDTO location,
                SystemDTO system,
                DemandDTO demand,
                EconomicsDTO economics) {

        public record LocationDTO(
                        String label,
                        double lat,
                        double lon,
                        String country,
                        String countryCode) {
        }

        public record SystemDTO(
                        double installedCapacityKw,
                        double performanceRatio,
                        double degradationRateAnnualPct,
                        double availabilityPct,
                        LossesPctDTO lossesPct) {
        }

        public record LossesPctDTO(
                        double inverter,
                        double temperature,
                        double wiring,
                        double soiling,
                        double other) {
        }

        public record DemandDTO(
                        double annualConsumptionKwh,
                        List<Double> monthlyConsumptionKwh) {
        }

        public record EconomicsDTO(
                        String currency,
                        double capexTotal,
                        double opexAnnual,
                        double electricityPurchasePricePerKwh,
                        double exportPricePerKwh,
                        double discountRatePct,
                        int projectLifetimeYears) {
        }
}
