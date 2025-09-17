package com.renewsim.backend.user_service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.user_service.application.port.in.CreateUserUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.service.UserPolicy;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.mapper.UserMapper;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public UserResponse createUser(UserCreateRequest req) {
        log.info("Start creating user with email={} username={}", req.email(), req.username());

        String username = UserPolicy.normalizeUsername(req.username());
        String email = UserPolicy.normalizeEmail(req.email());

        if (userRepositoryPort.existsByUsername(username) || userRepositoryPort.existsByEmail(email)) {
            log.warn("User creation failed: username={} or email={} already exists", username, email);
            throw new DataIntegrityViolationException("User with same username or email already exists");
        }

        User user = new User(
                null,
                username,
                email,
                true,
                Set.of("USER"),
                null,
                null,
                req.passwordHash());

        try {
            User saved = userRepositoryPort.save(user);
            log.info("User created successfully id={} username={}", saved.id(), saved.username());
            return UserMapper.toResponse(saved);
        } catch (Exception e) {
            log.error("Unexpected error while creating user email={} username={}", email, username, e);
            throw e;
        }
    }
}
