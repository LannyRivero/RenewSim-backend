package com.renewsim.backend.simulation_service.shared.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class SimulationFormatUtils {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private SimulationFormatUtils() {
    }

    public static String firstNonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public static String formatDate(LocalDateTime value) {
        return value == null ? null : ISO_FORMATTER.format(value) + "Z";
    }
}
