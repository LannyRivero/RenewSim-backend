package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.command.ChangeMyPasswordCommand;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeMyPasswordService")
class ChangeMyPasswordServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @InjectMocks private ChangeMyPasswordService service;

    private static final String CURRENT_HASH = new BCryptPasswordEncoder(12).encode("currentPass");
    private static final String NEW_HASH = new BCryptPasswordEncoder(12).encode("newPass123");

    private User buildUser(Long id) {
    return User.reconstitute(
        id, 
        "john@example.com", 
        CURRENT_HASH, 
        "John", 
        null,
        UserStatus.ACTIVE, 
        Set.of(RoleName.USER), 
        LocalDateTime.now(), 
        LocalDateTime.now(),
        true, 
        LocalDateTime.now()  
    );
}

    @Test
    @DisplayName("valid current password -> changes password and revokes refresh tokens")
    void changeMyPassword_valid_changesAndRevokes() {
        User user = buildUser(1L);

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPass", CURRENT_HASH)).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn(NEW_HASH);
        when(userRepositoryPort.save(any(User.class))).thenReturn(user);

        service.changeMyPassword(new ChangeMyPasswordCommand(1L, "currentPass", "newPass123"));

        verify(userRepositoryPort).save(any(User.class));
        verify(refreshTokenRepositoryPort).revokeAllByUserId(1L);
        verify(passwordEncoder).encode("newPass123");
    }

    @Test
    @DisplayName("wrong current password -> throws AuthenticationException")
    void changeMyPassword_wrongCurrentPassword_throws() {
        User user = buildUser(1L);

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", CURRENT_HASH)).thenReturn(false);

        assertThatThrownBy(() -> service.changeMyPassword(
                new ChangeMyPasswordCommand(1L, "wrongPass", "newPass123")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("incorrect");

        verify(userRepositoryPort, never()).save(any());
        verify(refreshTokenRepositoryPort, never()).revokeAllByUserId(any());
    }

    @Test
    @DisplayName("unknown userId -> throws UserNotFoundException")
    void changeMyPassword_unknownUser_throws() {
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeMyPassword(
                new ChangeMyPasswordCommand(99L, "pass", "newpass")))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("valid change -> refresh tokens are always revoked")
    void changeMyPassword_valid_alwaysRevokesRefreshTokens() {
        User user = buildUser(1L);

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPass", CURRENT_HASH)).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn(NEW_HASH);
        when(userRepositoryPort.save(any(User.class))).thenReturn(user);

        service.changeMyPassword(new ChangeMyPasswordCommand(1L, "currentPass", "newPass123"));

        verify(refreshTokenRepositoryPort, times(1)).revokeAllByUserId(1L);
    }
}