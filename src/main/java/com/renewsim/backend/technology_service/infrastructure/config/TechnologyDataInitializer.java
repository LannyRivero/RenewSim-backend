package com.renewsim.backend.technology_service.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.domain.model.Technology;

@Component
@Profile("dev")
public class TechnologyDataInitializer implements CommandLineRunner {

    private final TechnologyRepositoryPort repository;

    public TechnologyDataInitializer(TechnologyRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.findAll().isEmpty()) {
            repository.save(new Technology("Solar Panel", 0.85, 1200, 100, 15, 250, 6000, "SOLAR"));
            repository.save(new Technology("Wind Turbine", 0.75, 1500, 120, 10, 300, 8000, "WIND"));
            repository.save(new Technology("Hydro Generator", 0.90, 2500, 180, 5, 400, 10000, "HYDRO"));
            System.out.println("✅ [TechnologyDataInitializer] Development data loaded successfully");
        }
    }
}
