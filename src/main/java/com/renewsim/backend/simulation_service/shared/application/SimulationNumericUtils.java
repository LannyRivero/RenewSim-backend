package com.renewsim.backend.simulation_service.shared.application;

import java.util.List;

public final class SimulationNumericUtils {

    private SimulationNumericUtils() {
    }

    public static double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    public static List<Double> roundList(List<Double> values, int scale) {
        return values.stream().map(value -> round(value, scale)).toList();
    }

    public static double sum(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).sum();
    }

    public static List<Double> scale(List<Double> values, double factor) {
        return values.stream().map(value -> value * factor).toList();
    }

    public static double defaultNumber(Double value) {
        return value == null ? 0.0 : value;
    }
}
