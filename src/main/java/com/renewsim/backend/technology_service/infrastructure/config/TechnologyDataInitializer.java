package com.renewsim.backend.technology_service.infrastructure.config;

import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.domain.factory.TechnologyFactory;
import com.renewsim.backend.technology_service.domain.model.Technology;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("!test")
public class TechnologyDataInitializer implements CommandLineRunner {

    private final TechnologyRepositoryPort repository;

    public TechnologyDataInitializer(TechnologyRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        log.info("🚀 Starting TechnologyDataInitializer");

        if (repository.findAll().isEmpty()) {
            log.info("🧱 No technologies found. Loading initial dataset...");

            List<Technology> defaultTechnologies = List.of(
                    TechnologyFactory.create("Solar Panel", 85.0, 1200, 100, 15, 250, 6000, "SOLAR"),
                    TechnologyFactory.create("Wind Turbine", 70.0, 1500, 120, 10, 300, 8000, "EOLIC"),
                    TechnologyFactory.create("Hydro Generator", 90.0, 2500, 180, 5, 400, 10000, "HYDRO")
            );

            defaultTechnologies.forEach(tech -> {
                boolean exists = repository.existsByName(tech.getName());
                if (!exists) {
                    repository.save(tech);
                    log.info("✅ Inserted default technology: {}", tech.getName());
                } else {
                    log.warn("⚠️ Skipped duplicate technology: {}", tech.getName());
                }
            });

            log.info("✅ [TechnologyDataInitializer] Default technologies loaded successfully");
        } else {
            log.info("📦 Repository already contains technologies. Skipping initialization.");
        }
    }
}