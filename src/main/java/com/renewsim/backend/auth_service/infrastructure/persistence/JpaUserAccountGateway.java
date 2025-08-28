package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.role.Role;
import com.renewsim.backend.role.RoleName;
import com.renewsim.backend.role.RoleRepository;
import com.renewsim.backend.user.User;
import com.renewsim.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaUserAccountGateway implements UserAccountGateway {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private static String normalize(String username) {
        if (username == null) return null;
        return username.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public Optional<UserSnapshot> findByUsername(String username) {
        final String norm = normalize(username);
        return userRepository.findByUsername(norm).map(this::toSnapshot);
    }

    @Override
    public boolean existsByUsername(String username) {
        final String norm = normalize(username);
        return userRepository.existsByUsername(norm);
    }

    @Override
    @Transactional
    public void createUser(String username, String passwordHash, Set<RoleName> roles) {
        final String norm = normalize(username);
        if (norm == null || norm.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }

        Set<Role> roleEntities = roles.stream()
            .map(rn -> roleRepository.findByName(rn)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + rn)))
            .collect(Collectors.toUnmodifiableSet());

        User user = new User(norm, passwordHash, roleEntities);
        userRepository.save(user);
    }

    private UserSnapshot toSnapshot(User user) {
        Set<RoleName> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toUnmodifiableSet());
        return new UserSnapshot(user.getUsername(), user.getPassword(), roles);
    }
}

