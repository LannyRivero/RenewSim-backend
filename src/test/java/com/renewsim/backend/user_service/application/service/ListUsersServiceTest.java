package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListUsersServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private ListUsersService listUsersService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
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

        User user1 = new User(1L, "alice", "alice@mail.com", true, Set.of("USER"), null, null, "hashed1");
        User user2 = new User(2L, "bob", "bob@mail.com", true, Set.of("ADMIN"), null, null, "hashed2");

        Page<User> page = new PageImpl<>(List.of(user1, user2));
        when(userRepositoryPort.search(filters, PageRequest.of(0, 5))).thenReturn(page);

        PageResponse<UserResponse> result = listUsersService.listUsers(0, 5, filters);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).username()).isEqualTo("alice");
        assertThat(result.content().get(1).username()).isEqualTo("bob");
        assertThat(result.totalElements()).isEqualTo(2);
    }
}
