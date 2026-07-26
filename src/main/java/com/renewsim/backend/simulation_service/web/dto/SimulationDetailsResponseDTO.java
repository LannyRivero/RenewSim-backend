package com.renewsim.backend.simulation_service.web.dto;

import java.util.List;

public record SimulationDetailsResponseDTO(
                String id,
                String status,
                String createdAt,
                String updatedAt,
                String modelVersion,
                String technology,
                ResolvedLocationDTO location,
                SummaryDTO summary,
                CreateSolarSimulationRequestDTO input,
                TechnicalDTO technical,
                FinancialDTO financial,
                AssumptionsDTO assumptions,
                List<SimulationWarningDTO> warnings) {

        public record ResolvedLocationDTO(
                        String label,
                        String name,
                        String adminRegion,
                        String country,
                        String countryCode,
                        double lat,
                        double lon,
                        String timezone) {
        }

        public record SummaryDTO(
                        String recommendation,
                        String headline,
                        String summary,
                        List<RecommendationReasonDTO> reasons) {
        }

        public record RecommendationReasonDTO(String area, String severity, String message) {
        }

        public record TechnicalDTO(
                        double annualGenerationKwh,
                        List<Double> monthlyGenerationKwh,
                        double specificYieldKwhPerKwp,
                        double performanceRatio,
                        double capacityFactorPct,
                        double selfConsumptionRatePct,
                        double coverageRatePct,
                        ResourceSeriesDTO resource,
                        LossesSummaryDTO lossesPct,
                        List<MonthlyEnergyBalanceItemDTO> balanceByMonth) {
        }

        public record ResourceSeriesDTO(
                        String source,
                        String period,
                        List<Double> monthlyIrradianceKwhM2,
                        List<Double> monthlyTemperatureC) {
        }

        public record LossesSummaryDTO(
                        double inverter,
                        double temperature,
                        double wiring,
                        double soiling,
                        double other,
                        double total) {
        }

        public record MonthlyEnergyBalanceItemDTO(
                        String month,
                        double generationKwh,
                        double consumptionKwh,
                        double selfConsumedKwh,
                        double exportedKwh,
                        double importedKwh) {
        }

        public record FinancialDTO(
                        String currency,
                        double annualSavings,
                        double annualExportRevenue,
                        double netAnnualBenefit,
                        Double paybackYears,
                        Double discountedPaybackYears,
                        double npv,
                        Double irrPct,
                        double lcoePerKwh,
                        List<FinancialYearItemDTO> yearlyCashFlows) {
        }

        public record FinancialYearItemDTO(
                        int year,
                        double savings,
                        double exportRevenue,
                        double opex,
                        double replacementCost,
                        double netCashFlow,
                        double discountedCashFlow,
                        double cumulativeCashFlow) {
        }

        public record AssumptionsDTO(
                        double discountRatePct,
                        int projectLifetimeYears,
                        double degradationRateAnnualPct,
                        double electricityPurchasePricePerKwh,
                        double exportPricePerKwh,
                        String climateSource,
                        String climatePeriod) {
        }

        public record SimulationWarningDTO(String severity, String code, String message) {
        }
}
