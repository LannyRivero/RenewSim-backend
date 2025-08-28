package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.role.RoleName;
import java.util.Optional;
import java.util.Set;

public interface UserAccountGateway {

    Optional<UserSnapshot> findByUsername(String username);

    boolean existsByUsername(String username);

    void createUser(String username, String passwordHash, Set<RoleName> roles);

    record UserSnapshot(String username, String passwordHash, Set<RoleName> roles) {
    }
}
