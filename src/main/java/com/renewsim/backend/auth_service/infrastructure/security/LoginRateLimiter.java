package com.renewsim.backend.auth_service.infrastructure.security;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class LoginRateLimiter {   // ← public

    private static final class WindowCounter {
        final long windowStartEpochSec;
        final AtomicInteger count = new AtomicInteger(1);
        WindowCounter(long windowStartEpochSec) {
            this.windowStartEpochSec = windowStartEpochSec;
        }
    }

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final int windowSeconds;
    private final int maxAttempts;

    public LoginRateLimiter(int windowSeconds, int maxAttempts) { 
        this.windowSeconds = windowSeconds;
        this.maxAttempts = maxAttempts;
    }

    public boolean allow(String key) {
        Objects.requireNonNull(key, "key");
        final long nowSec = Instant.now().getEpochSecond();
        final long windowStart = nowSec - (nowSec % windowSeconds);

        counters.compute(key, (k, current) -> {
            if (current == null || current.windowStartEpochSec != windowStart) {
                return new WindowCounter(windowStart);
            }
            current.count.incrementAndGet();
            return current;
        });

        WindowCounter wc = counters.get(key);
        return wc.count.get() <= maxAttempts;
    }

    public int secondsUntilWindowReset() {
        final long nowSec = Instant.now().getEpochSecond();
        final long nextWindowStart = nowSec - (nowSec % windowSeconds) + windowSeconds;
        long delta = nextWindowStart - nowSec;
        return (int) Math.max(0, delta);
    }

    public void resetAll() {
        counters.clear();
    }
}

