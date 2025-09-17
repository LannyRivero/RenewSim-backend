package com.renewsim.backend.user_service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.port.in.GetUserUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.mapper.UserMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserService implements GetUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public UserResponse getUserById(Long id) {
        log.info("Fetching user by id={}", id);

        return userRepositoryPort.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public UserResponse getUserByUsernameOrEmail(String username, String email) {
        if ((username == null || username.isBlank()) && (email == null || email.isBlank())) {
            log.warn("Invalid request: both username and email are null/blank");
            throw new InvalidUserDataException("Either username or email must be provided");

        }

        if (username != null && !username.isBlank()) {
            log.info("Fetching user by username={}", username);
            return userRepositoryPort.findByUsername(username)
                    .map(UserMapper::toResponse)
                    .orElseThrow(() -> new UserNotFoundException("User with username '" + username + "' not found"));
        }

        log.info("Fetching user by email={}", email);
        return userRepositoryPort.findByEmail(email)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User with email '" + email + "' not found"));
    }

    @Override
    public User getDomainUserByUsernameOrEmail(String username, String email) {
        log.info("Fetching domain user by username={} or email={}", username, email);

        if (username != null && !username.isBlank()) {
            return userRepositoryPort.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User with username '" + username + "' not found"));
        }

        if (email != null && !email.isBlank()) {
            return userRepositoryPort.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User with email '" + email + "' not found"));
        }

        throw new InvalidUserDataException("Either username or email must be provided");

    }
}

