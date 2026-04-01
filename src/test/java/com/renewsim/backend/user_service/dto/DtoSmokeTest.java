package com.renewsim.backend.user_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.user_service.web.dto.PageResponse;
import com.renewsim.backend.user_service.web.dto.UserCredentialsDTO;
import com.renewsim.backend.user_service.web.dto.UserFilterRequest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoSmokeTest {

    @Test
    @DisplayName("should create and read PageResponse correctly")
    void testPageResponse() {
        PageResponse<String> response = new PageResponse<>(
                List.of("one", "two"),
                0,
                2,
                2L,
                1,
                true
        );

        assertThat(response.content()).containsExactly("one", "two");
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(2L);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.last()).isTrue();
    }

    @Test
    @DisplayName("should create and read UserCredentialsDTO correctly")
    void testUserCredentialsDTO() {
        UserCredentialsDTO dto = new UserCredentialsDTO(
                "john",
                "john@example.com",
                "securePass",
                Set.of(RoleName.USER, RoleName.ADMIN),
                true
        );

        assertThat(dto.username()).isEqualTo("john");
        assertThat(dto.email()).isEqualTo("john@example.com");
        assertThat(dto.passwordHash()).isEqualTo("securePass");
        assertThat(dto.roles()).contains(RoleName.USER, RoleName.ADMIN);
        assertThat(dto.enabled()).isTrue();
    }

    @Test
    @DisplayName("should create and read UserFilterRequest correctly")
    void testUserFilterRequest() {
        UserFilterRequest filter = new UserFilterRequest(
                "alice",
                "alice@example.com",
                true
        );

        assertThat(filter.username()).isEqualTo("alice");
        assertThat(filter.email()).isEqualTo("alice@example.com");
        assertThat(filter.enabled()).isTrue();
    }
}

