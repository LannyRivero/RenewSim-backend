package com.renewsim.backend.config;

import com.renewsim.backend.role.Role;
import com.renewsim.backend.role.RoleName;
import com.renewsim.backend.role.RoleRepository;
import com.renewsim.backend.user.User;
import com.renewsim.backend.user.UserRepository;
import com.renewsim.backend.technologyComparison.TechnologyComparison;
import com.renewsim.backend.technologyComparison.TechnologyComparisonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Configuration
public class GlobalDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(GlobalDataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TechnologyComparisonRepository technologyRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USER}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    public GlobalDataInitializer(RoleRepository roleRepository,
                                 UserRepository userRepository,
                                 TechnologyComparisonRepository technologyRepository,
                                 PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.technologyRepository = technologyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initRoles();
        initAdminUser();
        initTechnologies();
    }

    private void initRoles() {
        List<RoleName> roles = List.of(RoleName.USER, RoleName.ADMIN);

        roles.forEach(roleName -> {
            roleRepository.findByName(roleName).orElseGet(() -> {
                logger.info("Creating role: {}", roleName);
                return roleRepository.save(new Role(roleName));
            });
        });

        logger.info("✅ Roles initialized successfully.");
    }

    private void initAdminUser() {
        userRepository.findByUsername(adminUsername).ifPresentOrElse(
            user -> logger.info("ℹ️ Admin user already exists: {}", adminUsername),
            () -> {
                Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                        .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

                User admin = new User(
                        adminUsername,
                        passwordEncoder.encode(adminPassword),
                        Set.of(adminRole)
                );

                userRepository.save(admin);
                logger.info("✅ Admin user created successfully: {}", adminUsername);
            }
        );
    }

    private void initTechnologies() {
        List<TechnologyComparison> defaultTechnologies = List.of(
                // 🌞 Solar
                new TechnologyComparison("Solar PV Monocrystalline", 0.20, 950.0, 40.0,
                        "High efficiency, recyclable panels", 120.0, 1400.0, "SOLAR"),
                new TechnologyComparison("Solar PV Polycrystalline", 0.17, 750.0, 35.0,
                        "Lower efficiency but cheaper", 100.0, 1200.0, "SOLAR"),
                new TechnologyComparison("Solar Thermal Collector", 0.50, 1100.0, 60.0,
                        "Requires water system, good for heating", 150.0, 1600.0, "SOLAR"),

                // 🌬️ Wind
                new TechnologyComparison("Onshore Wind Turbine (2 MW)", 0.35, 1200.0, 80.0,
                        "Noise and visual impact", 250.0, 3000.0, "WIND"),
                new TechnologyComparison("Offshore Wind Turbine (5 MW)", 0.45, 2200.0, 150.0,
                        "Marine impact, expensive but stable", 450.0, 6000.0, "WIND"),

                // 💧 Hydro
                new TechnologyComparison("Small Hydro Plant (10 MW)", 0.40, 1000.0, 70.0,
                        "Local river impact", 200.0, 2500.0, "HYDRO"),
                new TechnologyComparison("Large Hydro Plant (100 MW)", 0.50, 3000.0, 200.0,
                        "High environmental impact", 800.0, 9000.0, "HYDRO"),

                // 🌱 Biomass
                new TechnologyComparison("Biomass Power Plant", 0.30, 1300.0, 90.0,
                        "CO₂ neutral if sustainable", 180.0, 2700.0, "BIOMASS"),

                // 🌍 Geothermal
                new TechnologyComparison("Geothermal Plant (Deep)", 0.45, 2500.0, 120.0,
                        "Stable base load, high drilling cost", 500.0, 7000.0, "GEOTHERMAL")
        );

        defaultTechnologies.forEach(tech -> {
            if (!technologyRepository.existsByTechnologyName(tech.getTechnologyName())) {
                technologyRepository.save(tech);
                logger.info("✅ Created technology: {}", tech.getTechnologyName());
            } else {
                logger.info("ℹ️ Technology already exists: {}", tech.getTechnologyName());
            }
        });

        logger.info("✅ Technologies initialization complete.");
    }
}
