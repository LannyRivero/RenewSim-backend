package com.renewsim.backend.user_service.application.port.out;

public interface ExistsUserPort {
    boolean existsByUsernameOrEmail(String username, String email);
}

