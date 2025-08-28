package com.renewsim.backend.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public final class ClientIpExtractor {

    public String extract(HttpServletRequest req) {
        return clientIp(req);
    }

    public static String clientIp(HttpServletRequest req) {
        String xfwd = header(req, "X-Forwarded-For");
        if (!xfwd.isBlank()) {
            return xfwd.split(",")[0].trim();
        }
        String realIp = header(req, "X-Real-IP");
        if (!realIp.isBlank())
            return realIp;
        String forwarded = header(req, "Forwarded");
        if (!forwarded.isBlank())
            return forwarded;

        return req.getRemoteAddr();
    }

    private static String header(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        return v == null ? "" : v.trim();
    }
}
