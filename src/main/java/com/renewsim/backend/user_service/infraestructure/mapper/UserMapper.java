package com.renewsim.backend.user_service.infraestructure.mapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserEntity e) {
        return new User(
                e.getId(),
                e.getUsername(),
                e.getEmail(),
                e.isEnabled(),
                csvToSet(e.getRolesCsv()),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    public static UserEntity toEntity(User d) {
        UserEntity e = new UserEntity();
        e.setId(d.id());
        e.setUsername(d.username());
        e.setEmail(d.email());
        e.setEnabled(d.enabled());
        e.setRolesCsv(setToCsv(d.roles()));
        e.setCreatedAt(d.createdAt());
        e.setUpdatedAt(d.updatedAt());
        return e;
    }

    // Domain -> DTO
    public static UserResponse toResponse(User d) {
        return new UserResponse(
                d.id(),
                d.username(),
                d.email(),
                d.enabled(),
                d.roles(),
                d.createdAt(),
                d.updatedAt());
    }

    public static User toDomain(UserCreateRequest request) {
        return new User(
                null,
                request.username(),
                request.email(),
                true,
                request.roles(),
                Instant.now(),
                Instant.now());
    }

    private static Set<String> csvToSet(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Set::copyOf));
    }

    private static String setToCsv(Set<String> set) {
        return set == null || set.isEmpty()
                ? ""
                : set.stream().sorted().collect(Collectors.joining(","));
    }
}
