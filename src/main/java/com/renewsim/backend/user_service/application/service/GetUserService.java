package com.renewsim.backend.user_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.renewsim.backend.user_service.application.port.in.GetUserUseCase;
import com.renewsim.backend.user_service.application.port.out.LoadUserPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

    private final LoadUserPort loadUserPort;

    @Override
    public UserResponse getUserById(Long id) {
        return loadUserPort.loadUserById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public UserResponse getUserByUsernameOrEmail(String username, String email) {
        if ((username == null || username.isBlank()) && (email == null || email.isBlank())) {
            throw new IllegalArgumentException("Either username or email must be provided");
        }

        if (username != null && !username.isBlank()) {
            return loadUserPort.loadUserByUsername(username)
                    .map(UserMapper::toResponse)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        return loadUserPort.loadUserByEmail(email)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public User getDomainUserByUsernameOrEmail(String username, String email) {
        return (username != null && !username.isBlank()
                ? loadUserPort.loadUserByUsername(username)
                : loadUserPort.loadUserByEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
