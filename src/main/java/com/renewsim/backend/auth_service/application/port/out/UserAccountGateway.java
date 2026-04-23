package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;

import java.util.Optional;
import java.util.Set;

public interface UserAccountGateway {
    Optional<UserSnapshot> findByUsername(String username);

    Optional<UserSnapshot> findByEmail(String email);

    Optional<UserSnapshot> findById(Long userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    UserSnapshot createUser(String fullName, String rawPassword, String email, Set<RoleName> roles);

    void activateUser(Long userId);
}