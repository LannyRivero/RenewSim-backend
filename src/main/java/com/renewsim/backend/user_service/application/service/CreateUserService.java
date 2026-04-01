package com.renewsim.backend.user_service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.user_service.application.port.in.CreateUserUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.service.UserPolicy;
import com.renewsim.backend.user_service.web.dto.UserCreateRequest;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import com.renewsim.backend.user_service.application.mapper.UserServiceMapper;
import com.renewsim.backend.shared.domain.vo.RoleName;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserServiceMapper mapper;
    private final PasswordEncoder passwordEncoder; // << Inyección

    private static final RoleName DEFAULT_ROLE = RoleName.USER;

    @Override
    public UserResponse createUser(UserCreateRequest req) {
        if (req.username() == null || req.username().isBlank() ||
                req.email() == null || req.email().isBlank() ||
                req.password() == null || req.password().isBlank()) {
            throw new InvalidUserDataException("Username, email and password must be provided");
        }

        String username = UserPolicy.normalizeUsername(req.username());
        String email = UserPolicy.normalizeEmail(req.email());

        try {
            log.info("Start creating user with email={} username={}", email, username);

            if (userRepositoryPort.existsByUsername(username) || userRepositoryPort.existsByEmail(email)) {
                log.warn("User creation failed: username={} or email={} already exists", username, email);
                throw new UserAlreadyExistsException(
                        "User with username '" + username + "' or email '" + email + "' already exists");
            }

            // Encriptar antes de persistir
            String encodedPassword = passwordEncoder.encode(req.password());

            User user = User.create(username, email, encodedPassword, Set.of(DEFAULT_ROLE));
            log.debug("Encoded password for {} = {}", username, encodedPassword);

            User saved = userRepositoryPort.save(user);
            log.info("User created successfully id={} username={}", saved.id(), saved.username());
            return mapper.toResponse(saved);

        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating user email={} username={}", email, username, e);
            throw e;
        }
    }
}
