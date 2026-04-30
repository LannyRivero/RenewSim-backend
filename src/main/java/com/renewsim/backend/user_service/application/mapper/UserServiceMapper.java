package com.renewsim.backend.user_service.application.mapper;

import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import com.renewsim.backend.user_service.web.dto.UserCreateRequest;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import com.renewsim.backend.user_service.infrastructure.persistence.entity.UserEntity;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.shared.domain.vo.RoleName;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", implementationName = "UserServiceMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserServiceMapper {

    default User toDomain(UserEntity entity) {
        if (entity == null)
            return null;
        return User.reconstitute(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getPhone(),
                UserStatus.valueOf(entity.getStatus().name()),
                mapRoleEntitiesToRoleNames(entity.getRoles()),
                entity.getCreatedAt() != null
                        ? toLocalDateTime(entity.getCreatedAt())
                        : LocalDateTime.now(),
                toLocalDateTime(entity.getActivatedAt()),
                entity.isEmailVerified(),
                toLocalDateTime(entity.getEmailVerifiedAt()));
    }

    default UserEntity toEntity(User domain) {
        if (domain == null)
            return null;
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setFullName(domain.getFullName());
        entity.setPhone(domain.getPhone());
        entity.setStatus(UserStatus.valueOf(domain.getStatus().name()));
        entity.setActivatedAt(toInstant(domain.getActivatedAt()));
        entity.setEmailVerified(domain.isEmailVerified());
        entity.setEmailVerifiedAt(toInstant(domain.getEmailVerifiedAt()));
        return entity;
    }

    default UserResponse toResponse(User domain) {
        if (domain == null)
            return null;
        return new UserResponse(
                domain.getId(),
                domain.getEmail(),
                domain.getEmail(),
                domain.getFullName(),
                domain.getPhone(),
                domain.getStatus().name(),
                mapRoleNamesToStrings(domain.getRoles()),
                toInstant(domain.getCreatedAt()),
                toInstant(domain.getActivatedAt()));
    }

    default User toDomain(UserCreateRequest request, String hashedPassword) {
        if (request == null)
            return null;
        return User.create(
                request.email(),
                hashedPassword,
                request.fullName(),
                request.phone(),
                Set.of(RoleName.USER));
    }

    default Set<RoleName> mapRoleEntitiesToRoleNames(Set<RoleEntity> roleEntities) {
        if (roleEntities == null || roleEntities.isEmpty())
            return Set.of();
        return roleEntities.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
    }

    default Set<String> mapRoleNamesToStrings(Set<RoleName> roleNames) {
        if (roleNames == null || roleNames.isEmpty())
            return Set.of();
        return roleNames.stream()
                .map(RoleName::name)
                .collect(Collectors.toSet());
    }

    default LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    default Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toInstant(ZoneOffset.UTC);
    }
}