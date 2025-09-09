package com.renewsim.backend.auth_service.web.controller;

import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/test-errors")
@Validated
class TestErrorThrowingController {

    @GetMapping("/auth")
    public void auth() {
        throw new AuthenticationException("Auth failed");
    }

    @GetMapping("/bad-credentials")
    public void badCredentials() {
        throw new org.springframework.security.authentication.BadCredentialsException("bad creds");
    }

    @GetMapping("/forbidden")
    public void forbidden() {
        throw new RuntimeException(new AccessDeniedException("nope"));
    }

   @GetMapping("/conflict")
public void conflict() {
    throw new ResourceConflictException("AUTH_USERNAME_CONFLICT", "user exists");
}


    @GetMapping("/generic")
    public void generic() {
        throw new RuntimeException("boom");
    }

    @PostMapping(value = "/invalid-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void invalidJson(@RequestBody String body) {
    }

    @GetMapping("/validation")
    public void validation(@RequestParam @NotBlank String q) {
    }

    @GetMapping("/too-many")
    public void tooMany() {
        throw new TooManyRequestsException("Too many", 30);
    }

    static class TooManyRequestsException extends RuntimeException {
        private final Integer retryAfterSeconds;
        public TooManyRequestsException(String msg, Integer s) { super(msg); this.retryAfterSeconds = s; }
        public Integer getRetryAfterSeconds() { return retryAfterSeconds; }
    }
}

