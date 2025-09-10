package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.user_service.application.port.in.ExistsUserUseCase;
import com.renewsim.backend.user_service.application.port.out.ExistsUserPort;

public class ExistsUserService implements ExistsUserUseCase {
    private final ExistsUserPort existsUserPort;

    public ExistsUserService(ExistsUserPort existsUserPort) {
        this.existsUserPort = existsUserPort;
    }

    @Override
    public boolean existsByUsernameOrEmail(String username, String email) {
        return existsUserPort.existsByUsernameOrEmail(username, email);
    }

}
