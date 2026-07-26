package com.renewsim.backend.simulation_service.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateSimulationRequestDTO(
                @NotBlank @Size(min = 2, max = 120) String name,
                @NotBlank String technology,
                @NotNull @Valid LocationDTO location,
                @NotNull @Valid SystemDTO system,
                @NotNull @Valid DemandDTO demand,
                @NotNull @Valid EconomicsDTO economics) {

        public record LocationDTO(
                        @NotBlank String label,
                        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") double lat,
                        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") double lon,
                        @NotBlank String country,
                        @NotBlank String countryCode) {
        }

        public record SystemDTO(
                        @Positive double installedCapacityKw,
                        @DecimalMin(value = "0.0", inclusive = false) @DecimalMax(value = "1.0") double performanceRatio,
                        @DecimalMin("0.0") @DecimalMax("5.0") double degradationRateAnnualPct,
                        @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("100.0") double availabilityPct,
                        @NotNull @Valid LossesPctDTO lossesPct) {
        }

        public record LossesPctDTO(
                        @PositiveOrZero double inverter,
                        @PositiveOrZero double temperature,
                        @PositiveOrZero double wiring,
                        @PositiveOrZero double soiling,
                        @PositiveOrZero double other) {
        }

        public record DemandDTO(
                        @PositiveOrZero double annualConsumptionKwh,
                        @NotEmpty @Size(min = 12, max = 12) List<@PositiveOrZero Double> monthlyConsumptionKwh) {
        }

        public record EconomicsDTO(
                        @NotBlank String currency,
                        @PositiveOrZero double capexTotal,
                        @PositiveOrZero double opexAnnual,
                        @PositiveOrZero double electricityPurchasePricePerKwh,
                        @PositiveOrZero double exportPricePerKwh,
                        @PositiveOrZero double discountRatePct,
                        @PositiveOrZero int projectLifetimeYears) {
        }
}
