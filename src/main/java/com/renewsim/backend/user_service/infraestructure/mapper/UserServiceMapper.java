package com.renewsim.backend.user_service.infraestructure.mapper;

import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(
    componentModel = "spring",
    implementationName = "UserServiceMapperImpl",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserServiceMapper {

    // -------- Entity -> Domain --------
    @Mapping(target = "roles", source = "rolesCsv")  // Usa csvToSet automáticamente
    User toDomain(UserEntity entity);

    // -------- Domain -> Entity --------
    @Mapping(target = "rolesCsv", source = "roles")  // Usa setToCsv automáticamente
    UserEntity toEntity(User domain);

    // -------- Domain -> DTO --------
    UserResponse toResponse(User domain);

    // -------- CreateRequest -> Domain --------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "roles", expression = "java(java.util.Set.of(\"USER\"))")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    User toDomain(UserCreateRequest request);

    // -------- Helpers --------
    default Set<String> csvToSet(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    default String setToCsv(Set<String> set) {
        return set == null || set.isEmpty()
                ? ""
                : set.stream().sorted().collect(java.util.stream.Collectors.joining(","));
    }
}


