package com.renewsim.backend.auth_service.infrastructure.security;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class OtpRateLimiter {

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

    public OtpRateLimiter(int windowSeconds, int maxAttempts) {
        if (windowSeconds <= 0) {
            throw new IllegalArgumentException("windowSeconds must be > 0");
        }
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be > 0");
        }
        this.windowSeconds = windowSeconds;
        this.maxAttempts = maxAttempts;
    }

    public boolean tryAcquire(String email) {
        Objects.requireNonNull(email, "email");
        String key = email.trim().toLowerCase();
        return allow(key);
    }

    public int secondsUntilReset(String email) {
        Objects.requireNonNull(email, "email");
        String key = email.trim().toLowerCase();
        WindowCounter wc = counters.get(key);
        if (wc == null) {
            return 0;
        }
        final long nowSec = Instant.now().getEpochSecond();
        final long windowEnd = wc.windowStartEpochSec + windowSeconds;
        return (int) Math.max(0, windowEnd - nowSec);
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

    public int getCurrentAttempts(String email) {
        Objects.requireNonNull(email, "email");
        String key = email.trim().toLowerCase();
        WindowCounter wc = counters.get(key);
        if (wc == null) {
            return 0;
        }
        final long nowSec = Instant.now().getEpochSecond();
        final long windowStart = nowSec - (nowSec % windowSeconds);
        if (wc.windowStartEpochSec != windowStart) {
            return 0;
        }
        return wc.count.get();
    }

    public void reset(String email) {
        if (email != null) {
            String key = email.trim().toLowerCase();
            counters.remove(key);
        }
    }

    public void resetAll() {
        counters.clear();
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}