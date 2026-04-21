package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.command.UpdateMyProfileCommand;
import com.renewsim.backend.user_service.application.mapper.UserServiceMapper;
import com.renewsim.backend.user_service.application.port.in.UpdateMyProfileUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateMyProfileService implements UpdateMyProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserServiceMapper mapper;

    @Override
    public UserResponse updateMyProfile(UpdateMyProfileCommand command) {
        User user = userRepositoryPort.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User with id " + command.userId() + " not found"));

        user.updateProfile(command.fullName(), command.phone());

        User saved = userRepositoryPort.save(user);
        log.info("Profile updated for userId={}", command.userId());
        return mapper.toResponse(saved);
    }
}