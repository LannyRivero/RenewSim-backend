package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.command.ChangeMyPasswordCommand;
import com.renewsim.backend.user_service.application.port.in.ChangeMyPasswordUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChangeMyPasswordService implements ChangeMyPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Override
    public void changeMyPassword(ChangeMyPasswordCommand command) {
        User user = userRepositoryPort.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User with id " + command.userId() + " not found"));

        if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        String newHash = passwordEncoder.encode(command.newPassword()); 
        user.changePassword(newHash);
        userRepositoryPort.save(user);

        // Revoke all refresh tokens — force re-login after password change
        refreshTokenRepositoryPort.revokeAllByUserId(command.userId());

        log.info("Password changed for userId={}", command.userId());
    }
}