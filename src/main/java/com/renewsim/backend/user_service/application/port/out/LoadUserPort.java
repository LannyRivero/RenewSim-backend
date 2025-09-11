package com.renewsim.backend.user_service.application.port.out;

import java.util.Optional;

import com.renewsim.backend.user_service.domain.model.User;

public interface LoadUserPort {
    Optional<User> loadUserById(Long id);
    Optional<User> loadUserByUsername(String username);
    Optional<User> loadUserByEmail(String email);
}
