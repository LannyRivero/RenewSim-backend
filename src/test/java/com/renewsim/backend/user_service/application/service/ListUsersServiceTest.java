package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.user_service.application.mapper.UserServiceMapper;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import com.renewsim.backend.user_service.web.dto.PageResponse;
import com.renewsim.backend.user_service.web.dto.UserFilterRequest;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUsersServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private UserServiceMapper mapper;

    @InjectMocks
    private ListUsersService listUsersService;

    private static final String VALID_HASH = new BCryptPasswordEncoder(12).encode("pass");

    private User buildUser(Long id, String email) {
        return User.reconstitute(
                id,
                "john@example.com",
                VALID_HASH,
                "John",
                null,
                UserStatus.ACTIVE,
                Set.of(RoleName.USER),
                LocalDateTime.now(),
                LocalDateTime.now(),
                true,
                LocalDateTime.now());
    }

    private UserResponse buildResponse(Long id, String username, String email) {
        return new UserResponse(id, username, email, "Name", null,
                "ACTIVE", Set.of("USER"), null, null);
    }

    @Test
    @DisplayName("should throw exception when page index is negative")
    void testNegativePageIndex() {
        var filters = new UserFilterRequest("john", "mail@test.com", true);
        assertThatThrownBy(() -> listUsersService.listUsers(-1, 10, filters))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Page index must not be negative");
    }

    @Test
    @DisplayName("should throw exception when page size <= 0")
    void testInvalidPageSize() {
        var filters = new UserFilterRequest(null, null, null);
        assertThatThrownBy(() -> listUsersService.listUsers(0, 0, filters))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Page size must be greater than zero");
    }

    @Test
    @DisplayName("should return mapped users when results exist")
    void testUsersFound() {
        var filters = new UserFilterRequest("alice", null, true);

        User user1 = buildUser(1L, "alice@mail.com");
        User user2 = buildUser(2L, "bob@mail.com");

        UserResponse resp1 = buildResponse(1L, "alice", "alice@mail.com");
        UserResponse resp2 = buildResponse(2L, "bob", "bob@mail.com");

        var page = new PageImpl<>(List.of(user1, user2));
        when(userRepositoryPort.search(filters, PageRequest.of(0, 5))).thenReturn(page);
        when(mapper.toResponse(user1)).thenReturn(resp1);
        when(mapper.toResponse(user2)).thenReturn(resp2);

        PageResponse<UserResponse> result = listUsersService.listUsers(0, 5, filters);

        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
    }
}