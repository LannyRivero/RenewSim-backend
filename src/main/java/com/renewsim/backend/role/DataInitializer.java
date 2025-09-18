package com.renewsim.backend.role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.renewsim.backend.role_service.domain.model.RoleName;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        List<RoleName> roles = List.of(RoleName.USER, RoleName.ADMIN);

        roles.forEach(roleName -> {
            roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    logger.info("Creating role: {}", roleName);
                    return roleRepository.save(new Role(roleName));
                });
        });

        logger.info("✅ Roles initialized successfully.");
    }
}

