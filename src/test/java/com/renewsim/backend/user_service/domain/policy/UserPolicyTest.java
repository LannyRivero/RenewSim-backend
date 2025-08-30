package com.renewsim.backend.user_service.domain.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.renewsim.backend.user_service.domain.service.UserPolicy;

import static org.assertj.core.api.Assertions.assertThat;

class UserPolicyTest {

    @Test
    @DisplayName("normalizeUsername should return null when input is null")
    void normalizeUsername_nullInput_returnsNull() {
        assertThat(UserPolicy.normalizeUsername(null)).isNull();
    }

    @Test
    @DisplayName("normalizeUsername should trim and lowercase input")
    void normalizeUsername_trimAndLowercase() {
        assertThat(UserPolicy.normalizeUsername("  Alice  ")).isEqualTo("alice");
    }

    @Test
    @DisplayName("normalizeUsername should handle already lowercase")
    void normalizeUsername_alreadyLowercase() {
        assertThat(UserPolicy.normalizeUsername("bob")).isEqualTo("bob");
    }

    @Test
    @DisplayName("normalizeEmail should return null when input is null")
    void normalizeEmail_nullInput_returnsNull() {
        assertThat(UserPolicy.normalizeEmail(null)).isNull();
    }

    @Test
    @DisplayName("normalizeEmail should trim and lowercase input")
    void normalizeEmail_trimAndLowercase() {
        assertThat(UserPolicy.normalizeEmail("  Alice@Mail.COM  ")).isEqualTo("alice@mail.com");
    }

    @Test
    @DisplayName("normalizeEmail should handle already normalized email")
    void normalizeEmail_alreadyNormalized() {
        assertThat(UserPolicy.normalizeEmail("user@mail.com")).isEqualTo("user@mail.com");
    }
}
