package com.renewsim.backend.auth_service.web.dto;

import com.renewsim.backend.auth_service.infrastructure.security.LoginUsernameProbe;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoginUsernameProbeTest {

    @Test
    public void getUsername_WhenUsernameIsSet_ReturnsUsername() {
        LoginUsernameProbe probe = new LoginUsernameProbe();
        probe.setUsername("testUser");
        assertEquals("testUser", probe.getUsername());
    }

    @Test
    public void getUsername_WhenUsernameIsNull_ReturnsEmail() {
        LoginUsernameProbe probe = new LoginUsernameProbe();
        probe.setEmail("test@example.com");
        assertEquals("test@example.com", probe.getUsername());
    }

    @Test
    public void getUsername_WhenBothFieldsAreSet_ReturnsUsername() {
        LoginUsernameProbe probe = new LoginUsernameProbe();
        probe.setUsername("testUser");
        probe.setEmail("test@example.com");
        assertEquals("testUser", probe.getUsername());
    }

    @Test
    public void getUsername_WhenBothFieldsAreNull_ReturnsNull() {
        LoginUsernameProbe probe = new LoginUsernameProbe();
        assertNull(probe.getUsername());
    }
}