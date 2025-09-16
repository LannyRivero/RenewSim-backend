package com.renewsim.backend.config;

import com.renewsim.backend.role.Role;
import com.renewsim.backend.role.RoleName;
import com.renewsim.backend.role.RoleRepository;
import com.renewsim.backend.user.User;
import com.renewsim.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USER}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Ensure roles exist
        List<RoleName> roles = List.of(RoleName.USER, RoleName.ADMIN);

        roles.forEach(roleName -> {
            roleRepository.findByName(roleName).orElseGet(() -> {
                logger.info("Creating role: {}", roleName);
                return roleRepository.save(new Role(roleName));
            });
        });

        logger.info("✅ Roles initialized successfully.");

        // 2. Ensure admin user exists
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
}
