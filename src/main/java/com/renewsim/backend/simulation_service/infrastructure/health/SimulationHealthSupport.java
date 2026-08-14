package com.renewsim.backend.simulation_service.infrastructure.health;

import java.net.URI;

final class SimulationHealthSupport {

    private SimulationHealthSupport() {
    }

    static boolean hasValidHttpUri(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = normalize(uri.getScheme());
            return (scheme.equals("http") || scheme.equals("https")) && uri.getHost() != null;
        } catch (Exception ex) {
            return false;
        }
    }

    static boolean isMissingOrDummy(String key) {
        String normalized = normalize(key);
        return normalized.isBlank() || normalized.equals("dummy-key");
    }

    static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
