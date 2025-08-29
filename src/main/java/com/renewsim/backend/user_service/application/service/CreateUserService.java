package com.renewsim.backend.user_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.renewsim.backend.user_service.application.port.in.CreateUserUseCase;
import com.renewsim.backend.user_service.application.port.out.ExistsUserPort;
import com.renewsim.backend.user_service.application.port.out.SaveUserPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.service.UserPolicy;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.mapper.UserMapper;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

    private final SaveUserPort saveUserPort;
    private final ExistsUserPort existsUserPort;

    @Override
    public UserResponse create(UserCreateRequest req) {
        String username = UserPolicy.normalizeUsername(req.username());
        String email = req.email().trim().toLowerCase();

        if (existsUserPort.existsByUsernameOrEmail(username, email)) {
            throw new DataIntegrityViolationException("User with same username or email already exists");
        }

        User user = new User(
                null,
                username,
                email,
                true,
                Set.copyOf(req.roles()),
                null,
                null);

        User saved = saveUserPort.save(user);
        return UserMapper.toResponse(saved);
    }
}
