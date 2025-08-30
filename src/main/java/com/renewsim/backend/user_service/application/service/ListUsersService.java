package com.renewsim.backend.user_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.renewsim.backend.user_service.application.port.in.ListUsersUseCase;
import com.renewsim.backend.user_service.application.port.out.SearchUserPort;
import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class ListUsersService implements ListUsersUseCase {

    private final SearchUserPort searchUserPort; 
    @Override
    public PageResponse<UserResponse> listUsers(int page, int size, UserFilterRequest filters) {
        var p = searchUserPort.search(filters.username(), filters.email(), filters.enabled(), page, size);

        var content = p.getContent().stream()
                .map(UserMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.isLast()
        );
    }
}

