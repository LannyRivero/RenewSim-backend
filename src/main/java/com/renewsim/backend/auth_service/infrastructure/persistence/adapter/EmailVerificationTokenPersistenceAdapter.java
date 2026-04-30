package com.renewsim.backend.auth_service.infrastructure.persistence.adapter;

import com.renewsim.backend.auth_service.application.mapper.EmailVerificationTokenMapper;
import com.renewsim.backend.auth_service.application.port.out.EmailVerificationTokenRepository;
import com.renewsim.backend.auth_service.domain.model.EmailVerificationToken;
import com.renewsim.backend.auth_service.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import com.renewsim.backend.auth_service.infrastructure.persistence.repo.EmailVerificationTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Persistence adapter for email verification tokens.
 * Implements the repository port using JPA.
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationTokenPersistenceAdapter implements EmailVerificationTokenRepository {

    private final EmailVerificationTokenJpaRepository jpaRepository;

    @Override
    @Transactional
    public EmailVerificationToken save(EmailVerificationToken token) {
        EmailVerificationTokenEntity entity;
        
        if (token.getId() != null) {
            // Update existing token
            entity = jpaRepository.findById(token.getId())
                .orElseThrow(() -> new IllegalStateException("Token not found for update: " + token.getId()));
            EmailVerificationTokenMapper.updateEntity(entity, token);
        } else {
            // Create new token
            entity = EmailVerificationTokenMapper.toEntity(token);
        }
        
        EmailVerificationTokenEntity saved = jpaRepository.save(entity);
        return EmailVerificationTokenMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailVerificationToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
            .map(EmailVerificationTokenMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailVerificationToken> findLatestByUserId(Long userId) {
        return jpaRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
            .map(EmailVerificationTokenMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsValidTokenForUser(Long userId) {
        return jpaRepository.existsValidTokenForUser(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public int deleteExpiredAndVerifiedTokens(LocalDateTime before) {
        return jpaRepository.deleteExpiredAndVerifiedTokens(before);
    }
}