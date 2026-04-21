package com.renewsim.backend.auth_service.web.dto;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserSnapshot factory methods")
class UserSnapshotTest {

    private static final Long ID = 1L;
    private static final String USERNAME = "john";
    private static final String FULL_NAME = "John Doe";
    private static final String HASH = "$2a$12$dummyHashxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    private static final String EMAIL = "john@example.com";
    private static final Set<RoleName> ROLES = Set.of(RoleName.USER);

    @Test
    @DisplayName("active() -> crea snapshot con status ACTIVE y enabled=true")
    void active_createsSnapshotWithActiveStatus() {
        UserSnapshot user = UserSnapshot.active(ID, USERNAME, FULL_NAME, HASH, EMAIL, ROLES);

        assertThat(user.id()).isEqualTo(ID);
        assertThat(user.username()).isEqualTo(USERNAME);
        assertThat(user.fullName()).isEqualTo(FULL_NAME);
        assertThat(user.passwordHash()).isEqualTo(HASH);
        assertThat(user.email()).isEqualTo(EMAIL);
        assertThat(user.roles()).isEqualTo(ROLES);
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.enabled()).isTrue();
    }

    @Test
    @DisplayName("disabled() -> crea snapshot con status INACTIVE y enabled=false")
    void disabled_createsSnapshotWithInactiveStatus() {
        UserSnapshot user = UserSnapshot.disabled(ID, USERNAME, FULL_NAME, HASH, EMAIL, ROLES);

        assertThat(user.id()).isEqualTo(ID);
        assertThat(user.username()).isEqualTo(USERNAME);
        assertThat(user.fullName()).isEqualTo(FULL_NAME);
        assertThat(user.passwordHash()).isEqualTo(HASH);
        assertThat(user.email()).isEqualTo(EMAIL);
        assertThat(user.roles()).isEqualTo(ROLES);
        assertThat(user.status()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.enabled()).isFalse();
    }
}