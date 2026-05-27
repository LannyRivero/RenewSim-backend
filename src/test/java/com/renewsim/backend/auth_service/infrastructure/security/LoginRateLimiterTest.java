package com.renewsim.backend.auth_service.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    @Test
    @DisplayName("allow should not block first 4 attempts")
    void allow_firstFourAttempts_notBlocked() {
        LoginRateLimiter limiter = new LoginRateLimiter(60, 4);
        String key = limiter.buildKey("127.0.0.1", null);

        assertThat(limiter.tryAcquire(key)).isTrue();
        assertThat(limiter.tryAcquire(key)).isTrue();
        assertThat(limiter.tryAcquire(key)).isTrue();
        assertThat(limiter.tryAcquire(key)).isTrue();
    }

    @Test
    @DisplayName("allow should block on 5th attempt when max is 4")
    void allow_fifthAttempt_blocked() {
        LoginRateLimiter limiter = new LoginRateLimiter(60, 4);
        String key = limiter.buildKey("127.0.0.1", null);

        limiter.tryAcquire(key);
        limiter.tryAcquire(key);
        limiter.tryAcquire(key);
        limiter.tryAcquire(key);

        assertThat(limiter.tryAcquire(key)).isFalse();
    }

    @Test
    @DisplayName("resetAll should allow attempts again after previous block")
    void resetAll_afterBlock_allowsAgain() {
        LoginRateLimiter limiter = new LoginRateLimiter(60, 4);
        String key = limiter.buildKey("127.0.0.1", null);

        limiter.tryAcquire(key);
        limiter.tryAcquire(key);
        limiter.tryAcquire(key);
        limiter.tryAcquire(key);
        assertThat(limiter.tryAcquire(key)).isFalse();

        limiter.resetAll();

        assertThat(limiter.tryAcquire(key)).isTrue();
    }
}
