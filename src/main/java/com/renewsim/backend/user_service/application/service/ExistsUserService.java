package com.renewsim.backend.user_service.application.service;

import org.springframework.stereotype.Service;

import com.renewsim.backend.user_service.application.port.in.ExistsUserUseCase;
import com.renewsim.backend.user_service.application.port.out.ExistsUserPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExistsUserService implements ExistsUserUseCase {
    private final ExistsUserPort existsUserPort;


    @Override
    public boolean existsByUsernameOrEmail(String username, String email) {
        return existsUserPort.existsByUsernameOrEmail(username, email);
    }

}
