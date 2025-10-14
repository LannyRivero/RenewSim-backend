package com.renewsim.backend.technology_service.domain.util;

import java.util.Locale;

/**
 * Utility class to standardize numeric formatting across Value Objects.
 * Ensures consistent use of dot (.) as decimal separator (Locale.US).
 */
public final class ValueObjectFormatter {

    private ValueObjectFormatter() {
        // Utility class – prevent instantiation
    }

    /**
     * Formats a double with one decimal and Locale.US.
     *
     * @param value numeric value
     * @param unit  measurement unit (e.g., "%", "MWh/year", "€", "tons")
     * @return formatted string (e.g., "85.0 %" or "5000.0 MWh/year")
     */
    public static String format(double value, String unit) {
        return String.format(Locale.US, "%.1f %s", value, unit);
    }

    /**
     * Formats a double with custom decimal precision.
     *
     * @param value    numeric value
     * @param decimals number of decimal places
     * @param unit     measurement unit
     * @return formatted string
     */
    public static String format(double value, int decimals, String unit) {
        String pattern = "%." + decimals + "f %s";
        return String.format(Locale.US, pattern, value, unit);
    }
}
