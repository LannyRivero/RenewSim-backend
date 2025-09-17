package com.renewsim.backend.user_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.user_service.application.port.in.ListUsersUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.mapper.UserMapper;
import com.renewsim.backend.shared.mapper.PageMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListUsersService implements ListUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public PageResponse<UserResponse> listUsers(int page, int size, UserFilterRequest filters) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        var pageable = PageRequest.of(page, size);
        var resultPage = userRepositoryPort.search(filters, pageable);

        return PageMapper.toPageResponse(resultPage, UserMapper::toResponse);
    }

    @Override
    public java.util.List<UserResponse> listAll() {
        return userRepositoryPort.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }
}


