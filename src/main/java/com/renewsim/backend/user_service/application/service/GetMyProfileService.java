package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.mapper.UserServiceMapper;
import com.renewsim.backend.user_service.application.port.in.GetMyProfileUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyProfileService implements GetMyProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserServiceMapper mapper;

    @Override
    public UserResponse getMyProfile(Long userId) {
        return userRepositoryPort.findById(userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
    }

    @Override
    public UserResponse getMyProfileByEmail(String email) {
        return userRepositoryPort.findByEmail(email)
                .map(mapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    }
}