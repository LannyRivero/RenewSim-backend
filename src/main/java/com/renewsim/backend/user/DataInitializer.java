package com.renewsim.backend.user;

import com.renewsim.backend.role.Role;
import com.renewsim.backend.role.RoleRepository;
import com.renewsim.backend.role.RoleName;
import com.renewsim.backend.user.User;
import com.renewsim.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USER}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public Runnable initAdminUser() {
        return () -> {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN)));

            userRepository.findByUsername(adminUsername).ifPresentOrElse(
                user -> System.out.println("ℹ️ Admin user already exists"),
                () -> {
                    User admin = new User(
                            adminUsername,
                            passwordEncoder.encode(adminPassword),
                            Set.of(adminRole)
                    );
                    userRepository.save(admin);
                    System.out.println("✅ Admin user created successfully");
                }
            );
        };
    }
}
