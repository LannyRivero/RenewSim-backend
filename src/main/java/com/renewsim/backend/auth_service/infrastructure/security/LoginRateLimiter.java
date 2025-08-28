package com.renewsim.backend.auth_service.infrastructure.security;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class LoginRateLimiter {

    public enum Strategy {
        IP,
        IP_USER
    }

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
    private final Strategy strategy;

    public LoginRateLimiter(int windowSeconds, int maxAttempts) {
        this(windowSeconds, maxAttempts, Strategy.IP);
    }

    public LoginRateLimiter(int windowSeconds, int maxAttempts, Strategy strategy) {
        if (windowSeconds <= 0)
            throw new IllegalArgumentException("windowSeconds must be > 0");
        if (maxAttempts <= 0)
            throw new IllegalArgumentException("maxAttempts must be > 0");
        this.windowSeconds = windowSeconds;
        this.maxAttempts = maxAttempts;
        this.strategy = strategy == null ? Strategy.IP : strategy;
    }

    public String buildKey(String ip, String username) {
        Objects.requireNonNull(ip, "ip");
        ip = ip.trim();
        if (ip.isEmpty())
            throw new IllegalArgumentException("ip is blank");

        if (strategy == Strategy.IP_USER) {
            String u = username == null ? "" : username.trim();
            if (!u.isEmpty()) {
                return ip + "|" + u.toLowerCase(Locale.ROOT);
            }
        }
        return ip;
    }

    public boolean tryAcquire(String key) {
        return allow(key);
    }

    public int secondsUntilReset(String key) {
        return secondsUntilWindowReset();
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

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Strategy getStrategy() {
        return strategy;
    }
}
