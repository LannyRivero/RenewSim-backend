package com.renewsim.backend.user_service.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.shared.exception.ResourceNotFoundException;
import com.renewsim.backend.user_service.application.port.in.ActivateUserUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivateUserService implements ActivateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public void activate(Long userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.activateWithEmailVerified();
        userRepositoryPort.save(user);
    }

}