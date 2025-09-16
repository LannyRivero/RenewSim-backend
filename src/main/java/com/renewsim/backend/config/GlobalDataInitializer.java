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
                new TechnologyComparison("Solar PV", 0.18, 800.0, 50.0,
                        "Low land impact, recyclable panels", 100.0, 1200.0, "SOLAR"),
                new TechnologyComparison("Wind Turbine", 0.35, 1200.0, 80.0,
                        "Noise, visual impact", 250.0, 3000.0, "WIND"),
                new TechnologyComparison("Hydroelectric", 0.45, 1500.0, 100.0,
                        "Affects river ecosystems", 400.0, 5000.0, "HYDRO")
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
