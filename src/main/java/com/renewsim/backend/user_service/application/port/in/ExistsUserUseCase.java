package com.renewsim.backend.user_service.application.port.in;

public interface ExistsUserUseCase {
    boolean existsByUsernameOrEmail(String username, String email);

}
