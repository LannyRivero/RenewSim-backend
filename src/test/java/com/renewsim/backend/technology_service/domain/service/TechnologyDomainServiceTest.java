package com.renewsim.backend.technology_service.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.renewsim.backend.technology_service.domain.model.Technology;

import java.util.List;
import java.util.Optional;

class TechnologyDomainServiceTest {

    private final TechnologyDomainService service = new TechnologyDomainService();

    @Test
    void shouldNormalizeValueCorrectly() {
        double result = service.normalize(50, 0, 100);
        assertEquals(0.5, result);
    }

    @Test
    void shouldReturnMostEfficientTechnology() {
        var solar = new Technology("Solar", 0.9, 1000, 100, 10, 500, 1500, "SOLAR");
        var wind = new Technology("Wind", 0.8, 800, 80, 15, 600, 1600, "WIND");

        Optional<Technology> best = service.findMostEfficient(List.of(solar, wind));

        assertTrue(best.isPresent());
        assertEquals("Wind", best.get().name());
    }

    @Test
    void shouldReturnEmptyWhenListIsEmpty() {
        assertTrue(service.findMostEfficient(List.of()).isEmpty());
    }
}
