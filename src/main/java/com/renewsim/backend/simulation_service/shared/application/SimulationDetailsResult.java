package com.renewsim.backend.simulation_service.shared.application;

import java.util.List;

public record SimulationDetailsResult(
                String id,
                String status,
                String createdAt,
                String updatedAt,
                String modelVersion,
                String technology,
                ResolvedLocation location,
                Summary summary,
                Input input,
                Technical technical,
                Financial financial,
                Assumptions assumptions,
                List<SimulationWarning> warnings) {

        public record ResolvedLocation(
                        String label,
                        String name,
                        String adminRegion,
                        String country,
                        String countryCode,
                        double lat,
                        double lon,
                        String timezone) {
        }

        public record Summary(String recommendation, String headline, String summary,
                        List<RecommendationReason> reasons) {
        }

        public record RecommendationReason(String area, String severity, String message) {
        }

        public record Input(
                        String name,
                        String technology,
                        Location location,
                        SystemSpec system,
                        Demand demand,
                        Economics economics) {
        }

        public record Location(String label, double lat, double lon, String country, String countryCode) {
        }

        public record SystemSpec(double installedCapacityKw, double performanceRatio, double degradationRateAnnualPct,
                        double availabilityPct, LossesPct lossesPct) {
        }

        public record LossesPct(double inverter, double temperature, double wiring, double soiling, double other) {
        }

        public record Demand(double annualConsumptionKwh, List<Double> monthlyConsumptionKwh) {
        }

        public record Economics(String currency, double capexTotal, double opexAnnual,
                        double electricityPurchasePricePerKwh,
                        double exportPricePerKwh, double discountRatePct, int projectLifetimeYears) {
        }

        public record Technical(double annualGenerationKwh, List<Double> monthlyGenerationKwh,
                        double specificYieldKwhPerKwp,
                        double performanceRatio, double capacityFactorPct, double selfConsumptionRatePct,
                        double coverageRatePct, ResourceSeries resource, LossesSummary lossesPct,
                        List<MonthlyEnergyBalanceItem> balanceByMonth) {
        }

        public record ResourceSeries(String source, String period, List<Double> monthlyIrradianceKwhM2,
                        List<Double> monthlyTemperatureC) {
        }

        public record LossesSummary(double inverter, double temperature, double wiring, double soiling, double other,
                        double total) {
        }

        public record MonthlyEnergyBalanceItem(String month, double generationKwh, double consumptionKwh,
                        double selfConsumedKwh, double exportedKwh, double importedKwh) {
        }

        public record Financial(String currency, double annualSavings, double annualExportRevenue,
                        double netAnnualBenefit,
                        Double paybackYears, Double discountedPaybackYears, double npv, Double irrPct,
                        double lcoePerKwh, List<FinancialYearItem> yearlyCashFlows) {
        }

        public record FinancialYearItem(int year, double savings, double exportRevenue, double opex,
                        double replacementCost, double netCashFlow, double discountedCashFlow,
                        double cumulativeCashFlow) {
        }

        public record Assumptions(double discountRatePct, int projectLifetimeYears, double degradationRateAnnualPct,
                        double electricityPurchasePricePerKwh, double exportPricePerKwh,
                        String climateSource, String climatePeriod) {
        }

        public record SimulationWarning(String severity, String code, String message) {
        }
}
