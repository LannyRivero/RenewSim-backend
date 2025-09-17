package com.renewsim.backend.user_service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.user_service.application.port.in.ExistsUserUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExistsUserService implements ExistsUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public boolean exists(Long id) {
        MDC.put("action", "existsById");
        MDC.put("userId", String.valueOf(id));
        try {
            boolean result = userRepositoryPort.existsById(id);
            log.info("Checked existence by id={}, exists={}", id, result);
            return result;
        } finally {
            MDC.clear();
        }
    }

    @Override
    public boolean existsByUsernameOrEmail(String username, String email) {
        MDC.put("action", "existsByUsernameOrEmail");
        MDC.put("username", username);
        MDC.put("email", email);
        try {
            if (username != null && !username.isBlank()) {
                boolean result = userRepositoryPort.existsByUsername(username);
                log.info("Checked existence by username={}, exists={}", username, result);
                return result;
            }
            if (email != null && !email.isBlank()) {
                boolean result = userRepositoryPort.existsByEmail(email);
                log.info("Checked existence by email={}, exists={}", email, result);
                return result;
            }
            log.warn("Invalid request: both username and email are null/blank");
            throw new InvalidUserDataException("Either username or email must be provided");
        } finally {
            MDC.clear();
        }
    }
}


