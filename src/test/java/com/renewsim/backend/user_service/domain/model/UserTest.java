package com.renewsim.backend.user_service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("should allow null roles without throwing exception")
    void testUserWithNullRoles() {
        User user = new User(1L, "john", "john@mail.com", true, null, null, null, "hash123");

        assertThat(user.roles()).isNull();
        assertThat(user.username()).isEqualTo("john");
    }

    @Test
    @DisplayName("should copy roles into unmodifiable set when provided")
    void testUserWithRoles() {
        Set<String> roles = Set.of("USER", "ADMIN");
        User user = new User(2L, "alice", "alice@mail.com", true, roles, null, null, "hash456");

        assertThat(user.roles()).containsExactlyInAnyOrder("USER", "ADMIN");
        assertThatThrownBy(() -> user.roles().add("NEW_ROLE"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should handle empty roles set correctly")
    void testUserWithEmptyRoles() {
        User user = new User(3L, "bob", "bob@mail.com", true, Set.of(), null, null, "hash789");

        assertThat(user.roles()).isEmpty();
    }
}
