package com.renewsim.backend.simulation_service.shared.domain.service;

import java.text.Normalizer;
import java.util.Locale;

public final class LocationNameNormalizer {

    private LocationNameNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFKD);
        return decomposed.replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .strip()
                .replaceAll("\\s+", " ");
    }
}
