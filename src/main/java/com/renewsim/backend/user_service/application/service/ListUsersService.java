package com.renewsim.backend.user_service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.user_service.application.port.in.ListUsersUseCase;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.mapper.UserMapper;
import com.renewsim.backend.shared.mapper.PageMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListUsersService implements ListUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserMapper mapper;

    @Override
    public PageResponse<UserResponse> listUsers(int page, int size, UserFilterRequest filters) {
        MDC.put("action", "listUsers");
        MDC.put("page", String.valueOf(page));
        MDC.put("size", String.valueOf(size));
        MDC.put("usernameFilter", filters.username());
        MDC.put("emailFilter", filters.email());
        MDC.put("enabledFilter", String.valueOf(filters.enabled()));

        try {
            if (page < 0) {
                log.warn("Invalid page index: {}", page);
                throw new InvalidUserDataException("Page index must not be negative");
            }
            if (size <= 0) {
                log.warn("Invalid page size: {}", size);
                throw new InvalidUserDataException("Page size must be greater than zero");
            }

            var pageable = PageRequest.of(page, size);
            var resultPage = userRepositoryPort.search(filters, pageable);

            log.info("Retrieved {} users out of total {}", 
                    resultPage.getNumberOfElements(), resultPage.getTotalElements());

            return PageMapper.toPageResponse(resultPage, mapper::toResponse);

        } finally {
            MDC.clear();
        }
    }

    @Override
    public List<UserResponse> listAll() {
        MDC.put("action", "listAll");
        try {
            log.info("Fetching all users");
            return userRepositoryPort.findAll().stream()
                    .map(mapper::toResponse)
                    .toList();
        } finally {
            MDC.clear();
        }
    }
}


