package com.renewsim.backend.user_service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.renewsim.backend.user_service.application.port.in.GetUserUseCase;
import com.renewsim.backend.user_service.application.port.out.LoadUserPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.mapper.UserMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

    private final LoadUserPort loadUserPort;

    @Override
    public UserResponse getUserById(Long id) {
        log.info("Fetching user by id={}", id);

        return loadUserPort.loadUserById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public UserResponse getUserByUsernameOrEmail(String username, String email) {
        if ((username == null || username.isBlank()) && (email == null || email.isBlank())) {
            log.warn("Invalid request: both username and email are null/blank");
            throw new IllegalArgumentException("Either username or email must be provided");
        }

        if (username != null && !username.isBlank()) {
            log.info("Fetching user by username={}", username);
            return loadUserPort.loadUserByUsername(username)
                    .map(UserMapper::toResponse)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        log.info("Fetching user by email={}", email);
        return loadUserPort.loadUserByEmail(email)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public User getDomainUserByUsernameOrEmail(String username, String email) {
        log.info("Fetching domain user by username={} or email={}", username, email);
        return (username != null && !username.isBlank()
                ? loadUserPort.loadUserByUsername(username)
                : loadUserPort.loadUserByEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
