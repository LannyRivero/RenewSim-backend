package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;

import java.util.ArrayList;
import java.util.List;

import static com.renewsim.backend.simulation_service.shared.application.SimulationMathUtils.round;

public final class FinancialCalculator {

    private FinancialCalculator() {
    }

    public record Result(SimulationDetailsResult.Financial financial, double npv, Double irrPct, Double paybackYears) {
    }

    public static Result calculate(
            CreateRealSimulationCommand command,
            double annualGeneration,
            double annualSelfConsumed,
            double annualExported) {

        double annualSavings = annualSelfConsumed * command.economics().electricityPurchasePricePerKwh();
        double annualExportRevenue = annualExported * command.economics().exportPricePerKwh();
        double netAnnualBenefit = annualSavings + annualExportRevenue - command.economics().opexAnnual();
        double discountRate = command.economics().discountRatePct() / 100.0;
        double degradationRate = command.system().degradationRateAnnualPct() / 100.0;
        List<SimulationDetailsResult.FinancialYearItem> yearlyCashFlows = new ArrayList<>();

        double cumulativeCashFlow = -command.economics().capexTotal();
        yearlyCashFlows.add(new SimulationDetailsResult.FinancialYearItem(0, 0.0, 0.0, 0.0, 0.0,
                round(-command.economics().capexTotal(), 2),
                round(-command.economics().capexTotal(), 2),
                round(cumulativeCashFlow, 2)));

        List<Double> irrFlows = new ArrayList<>();
        irrFlows.add(-command.economics().capexTotal());

        double npv = -command.economics().capexTotal();
        double discountedGeneration = 0.0;
        double discountedOpex = 0.0;
        Double paybackYears = null;
        Double discountedPaybackYears = null;
        double undiscountedCumulative = -command.economics().capexTotal();
        double discountedCumulative = -command.economics().capexTotal();

        for (int year = 1; year <= command.economics().projectLifetime().years(); year++) {
            double degradationFactor = Math.pow(1.0 - degradationRate, year - 1);
            double savings = annualSavings * degradationFactor;
            double exportRevenue = annualExportRevenue * degradationFactor;
            double opex = command.economics().opexAnnual();
            double netCashFlow = savings + exportRevenue - opex;
            double discountedCashFlow = netCashFlow / Math.pow(1.0 + discountRate, year);
            npv += discountedCashFlow;
            irrFlows.add(netCashFlow);
            discountedGeneration += (annualGeneration * degradationFactor) / Math.pow(1.0 + discountRate, year);
            discountedOpex += opex / Math.pow(1.0 + discountRate, year);

            double previousUndiscounted = undiscountedCumulative;
            double previousDiscounted = discountedCumulative;
            undiscountedCumulative += netCashFlow;
            discountedCumulative += discountedCashFlow;

            if (paybackYears == null && undiscountedCumulative >= 0.0 && netCashFlow > 0.0) {
                paybackYears = year - 1 + (-previousUndiscounted / netCashFlow);
            }
            if (discountedPaybackYears == null && discountedCumulative >= 0.0 && discountedCashFlow > 0.0) {
                discountedPaybackYears = year - 1 + (-previousDiscounted / discountedCashFlow);
            }

            cumulativeCashFlow += netCashFlow;
            yearlyCashFlows.add(new SimulationDetailsResult.FinancialYearItem(year,
                    round(savings, 2), round(exportRevenue, 2), round(opex, 2), 0.0,
                    round(netCashFlow, 2), round(discountedCashFlow, 2), round(cumulativeCashFlow, 2)));
        }

        Double irrPct = calculateIrr(irrFlows);
        double lcoePerKwh = discountedGeneration <= 0.0 ? 0.0
                : (command.economics().capexTotal() + discountedOpex) / discountedGeneration;

        return new Result(
                new SimulationDetailsResult.Financial(
                        command.economics().currency().value(), round(annualSavings, 2), round(annualExportRevenue, 2),
                        round(netAnnualBenefit, 2), paybackYears == null ? null : round(paybackYears, 2),
                        discountedPaybackYears == null ? null : round(discountedPaybackYears, 2),
                        round(npv, 2), irrPct == null ? null : round(irrPct, 2),
                        round(lcoePerKwh, 4), yearlyCashFlows),
                npv, irrPct, paybackYears);
    }

    private static Double calculateIrr(List<Double> cashFlows) {
        double low = -0.99;
        double high = 1.5;
        double npvLow = npvAtRate(cashFlows, low);
        double npvHigh = npvAtRate(cashFlows, high);
        if (Math.signum(npvLow) == Math.signum(npvHigh)) {
            return null;
        }
        for (int i = 0; i < 100; i++) {
            double mid = (low + high) / 2.0;
            double npvMid = npvAtRate(cashFlows, mid);
            if (Math.abs(npvMid) < 0.0001) {
                return mid * 100.0;
            }
            if (Math.signum(npvMid) == Math.signum(npvLow)) {
                low = mid;
                npvLow = npvMid;
            } else {
                high = mid;
            }
        }
        return ((low + high) / 2.0) * 100.0;
    }

    private static double npvAtRate(List<Double> cashFlows, double rate) {
        double npv = 0.0;
        for (int year = 0; year < cashFlows.size(); year++) {
            npv += cashFlows.get(year) / Math.pow(1 + rate, year);
        }
        return npv;
    }
}
