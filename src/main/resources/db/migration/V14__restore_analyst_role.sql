-- =====================================================
-- V14: Restore ANALYST role as a supported system role
-- =====================================================
-- V13 temporarily removed ANALYST while the shared role catalog did not support it.
-- ANALYST is now modeled in the shared enum and security configuration, so we
-- restore it here in an idempotent way for existing environments.

INSERT INTO roles (name, description, created_by, created_at, updated_at)
VALUES ('ANALYST', 'Analyst with advanced simulation and reporting capabilities', 'system', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    updated_at = CURRENT_TIMESTAMP(6);
