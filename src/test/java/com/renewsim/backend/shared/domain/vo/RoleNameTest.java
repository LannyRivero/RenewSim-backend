package com.renewsim.backend.shared.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleNameTest {

    @Test
    @DisplayName("ANALYST should expose the expected Spring Security authority")
    void analystAsAuthority() {
        assertThat(RoleName.ANALYST.asAuthority()).isEqualTo("ROLE_ANALYST");
    }

    @Test
    @DisplayName("ANALYST should be resolved back from a Spring Security authority")
    void analystFromAuthority() {
        assertThat(RoleName.fromAuthority("ROLE_ANALYST")).isEqualTo(RoleName.ANALYST);
    }
}
