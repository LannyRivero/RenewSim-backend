package com.renewsim.backend.auth_service.application.mapper;

import com.renewsim.backend.auth_service.domain.model.EmailVerificationToken;
import com.renewsim.backend.auth_service.infrastructure.persistence.entity.EmailVerificationTokenEntity;

/**
 * Mapper between EmailVerificationToken domain model and JPA entity.
 * Both use LocalDateTime, so no conversion needed.
 */
public class EmailVerificationTokenMapper {

    /**
     * Convert JPA entity to domain model.
     *
     * @param entity JPA entity
     * @return domain model
     */
    public static EmailVerificationToken toDomain(EmailVerificationTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        return new EmailVerificationToken(
            entity.getId(),
            entity.getUserId(),
            entity.getToken(),
            entity.getExpiresAt(),
            entity.getVerifiedAt(),
            entity.getCreatedAt()
        );
    }

    /**
     * Convert domain model to JPA entity.
     *
     * @param domain domain model
     * @return JPA entity
     */
    public static EmailVerificationTokenEntity toEntity(EmailVerificationToken domain) {
        if (domain == null) {
            return null;
        }

        EmailVerificationTokenEntity entity = new EmailVerificationTokenEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setToken(domain.getToken());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setVerifiedAt(domain.getVerifiedAt());
        entity.setCreatedAt(domain.getCreatedAt());

        return entity;
    }

    /**
     * Update entity with domain model values (for save operations).
     *
     * @param entity entity to update
     * @param domain domain model with new values
     */
    public static void updateEntity(EmailVerificationTokenEntity entity, EmailVerificationToken domain) {
        entity.setVerifiedAt(domain.getVerifiedAt());
        // Other fields are immutable after creation
    }
}