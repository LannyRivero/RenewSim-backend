package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.port.in.DeleteUserUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public void deleteUser(Long userId) {
        if (!userRepositoryPort.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " not found");
        }
        userRepositoryPort.deleteById(userId);
        log.info("User deleted userId={}", userId);
    }
}