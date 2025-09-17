package com.renewsim.backend.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUserNotFound_shouldReturn404() {
        UserNotFoundException ex = new UserNotFoundException("User with id 99999 not found");

        ResponseEntity<Map<String, String>> response = handler.handleUserNotFound(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("error", "User with id 99999 not found");
        assertThat(response.getBody()).containsEntry("status", "404");
    }
}

