package com.renewsim.backend.technology_service.domain.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.renewsim.backend.technology_service.domain.model.Technology;

public class TechnologyDomainService {

    /**
     * Normalizes a numeric value to a [0–1] scale.
     */
    public double normalize(double value, double min, double max) {
        if (max <= min) throw new IllegalArgumentException("Invalid normalization range");
        return (value - min) / (max - min);
    }

    /**
     * Returns the most efficient technology (efficiency vs total cost ratio).
     */
    public Optional<Technology> findMostEfficient(List<Technology> technologies) {
        return technologies == null || technologies.isEmpty()
                ? Optional.empty()
                : technologies.stream()
                .max(Comparator.comparingDouble(t ->
                        t.efficiency() / (t.installationCost() + t.maintenanceCost())));
    }
}

