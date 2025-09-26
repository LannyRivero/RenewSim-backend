package com.renewsim.backend.role_service.infrastructure.client;

import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.user_service.dto.UpdateUserRolesRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HttpUserServiceGateway implements UserServiceGateway {

    private final UserServiceClient userServiceClient;

    @Override
    public void updateUserRoles(Long userId, UpdateUserRolesRequestDTO request) {
        userServiceClient.updateUserRoles(userId, request);
    }
}

