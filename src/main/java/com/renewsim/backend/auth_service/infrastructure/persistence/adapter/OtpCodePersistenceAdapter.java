package com.renewsim.backend.auth_service.infrastructure.persistence.adapter;

import com.renewsim.backend.auth_service.application.port.out.OtpCodeRepositoryPort;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.infrastructure.persistence.entity.OtpCodeEntity;
import com.renewsim.backend.auth_service.infrastructure.persistence.repo.OtpCodeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OtpCodePersistenceAdapter implements OtpCodeRepositoryPort {

    private final OtpCodeJpaRepository repo;

    @Override
    @Transactional
    public OtpCode save(OtpCode otpCode) {
        OtpCodeEntity entity = toEntity(otpCode);
        OtpCodeEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OtpCode> findLatestValidByUserId(Long userId, OtpCode.Purpose purpose) {
        return repo.findFirstByUserIdAndPurposeAndVerifiedAtIsNullAndExpiresAtAfterOrderByIssuedAtDesc(
                userId,
                purpose,
                Instant.now())
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void invalidateAllByUserId(Long userId, OtpCode.Purpose purpose) {
        repo.invalidateAllByUserIdAndPurpose(userId, purpose, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countFailedAttempts(Long userId, OtpCode.Purpose purpose) {
        return repo.countFailedAttempts(userId, purpose, Instant.now());
    }

    private OtpCodeEntity toEntity(OtpCode domain) {
        OtpCodeEntity entity = new OtpCodeEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setCodeHash(domain.getCodeHash());
        entity.setPurpose(domain.getPurpose());
        entity.setIssuedAt(domain.getIssuedAt().toInstant(ZoneOffset.UTC));
        entity.setExpiresAt(domain.getExpiresAt().toInstant(ZoneOffset.UTC));
        if (domain.isUsed()) {
            entity.setVerifiedAt(domain.getIssuedAt().toInstant(ZoneOffset.UTC));
        }
        return entity;
    }

    private OtpCode toDomain(OtpCodeEntity entity) {
        return OtpCode.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getCodeHash(),
                entity.getPurpose(),
                LocalDateTime.ofInstant(entity.getIssuedAt(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(entity.getExpiresAt(), ZoneOffset.UTC),
                entity.isUsed());
    }
}