-- =====================================================
-- V7: Update roles seed syntax (MySQL 8.0+ compatibility)
-- =====================================================
-- Fixes deprecation warning: VALUES() function deprecated
-- Uses modern alias syntax for ON DUPLICATE KEY UPDATE
-- Data already exists from V5, this ensures future idempotency

INSERT INTO roles (name, description, created_by, created_at, updated_at)
VALUES
    ('USER', 'Standard user with basic simulation access', 'system', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    ('ADMIN', 'Administrator with full system access', 'system', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    ('ANALYST', 'Analyst with advanced simulation and reporting capabilities', 'system', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
AS new_values
ON DUPLICATE KEY UPDATE
    description = new_values.description,
    updated_at = CURRENT_TIMESTAMP(6);