package com.renewsim.backend.user_service.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.user_service.application.port.in.ExistsUserUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExistsUserService implements ExistsUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public boolean exists(Long id) {
        return userRepositoryPort.existsById(id);
    }

    @Override
    public boolean existsByUsernameOrEmail(String username, String email) {
        if (username != null) {
            return userRepositoryPort.existsByUsername(username);
        }
        if (email != null) {
            return userRepositoryPort.existsByEmail(email);
        }
        throw new IllegalArgumentException("Either username or email must be provided");
    }
}

