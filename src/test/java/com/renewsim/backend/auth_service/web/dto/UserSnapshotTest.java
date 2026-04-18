package com.renewsim.backend.auth_service.web.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Set;
import org.junit.Test;
import com.renewsim.backend.shared.domain.vo.RoleName;

public class UserSnapshotTest {

    @Test
    public void testActiveUserCreation() {
        Long id = 1L;
        String username = "testUser";
        String password = "hashedPassword";
        String email = "test@example.com";
        Set<RoleName> roles = Set.of(RoleName.USER);

        UserSnapshot user = UserSnapshot.active(id, username, password, email, roles);

        assertEquals(username, user.username());
        assertEquals(password, user.passwordHash());
        assertEquals(email, user.email());
        assertEquals(roles, user.roles());
        assertTrue(user.enabled());
    }

    @Test
    public void testDisabledUserCreation() {
        Long id = 1L;
        String username = "testUser";
        String password = "hashedPassword";
        String email = "test@example.com";
        Set<RoleName> roles = Set.of(RoleName.USER);

        UserSnapshot user = UserSnapshot.disabled(id, username, password, email, roles);

        assertEquals(username, user.username());
        assertEquals(password, user.passwordHash());
        assertEquals(email, user.email());
        assertEquals(roles, user.roles());
        assertFalse(user.enabled());
    }
}