-- =====================================================
-- V2: Create authentication tables
-- Requirements: 2.4, 5.1
-- =====================================================

-- =====================================================
-- REFRESH_TOKENS TABLE
-- =====================================================
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(512) NOT NULL UNIQUE COMMENT 'Refresh token value',
    user_id BIGINT NOT NULL,
    issued_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at TIMESTAMP(6) NOT NULL COMMENT 'Token expiration timestamp',
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP(6) NULL,
    device_info VARCHAR(255) COMMENT 'Device/browser information',
    ip_address VARCHAR(45) COMMENT 'IPv4 or IPv6 address',
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='JWT refresh tokens';

-- Compound index for token validation (Requirement 2.4)
CREATE INDEX idx_refresh_tokens_user_revoked ON refresh_tokens(user_id, revoked);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- =====================================================
-- TOKEN_BLACKLIST TABLE
-- =====================================================
CREATE TABLE token_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    jti VARCHAR(128) NOT NULL UNIQUE COMMENT 'JWT ID (jti claim)',
    user_id BIGINT NOT NULL,
    revoked_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at TIMESTAMP(6) NOT NULL COMMENT 'Original token expiration',
    reason VARCHAR(255) COMMENT 'Revocation reason',
    CONSTRAINT fk_token_blacklist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Revoked JWT tokens';

-- Index for blacklist checks (Requirement 5.1)
CREATE INDEX idx_token_blacklist_jti ON token_blacklist(jti);
CREATE INDEX idx_token_blacklist_expires_at ON token_blacklist(expires_at);

-- =====================================================
-- OTP_CODES TABLE
-- =====================================================
CREATE TABLE otp_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    code VARCHAR(10) NOT NULL COMMENT 'OTP code (6-10 digits)',
    purpose VARCHAR(50) NOT NULL COMMENT 'LOGIN, PASSWORD_RESET, EMAIL_VERIFICATION',
    issued_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at TIMESTAMP(6) NOT NULL,
    verified_at TIMESTAMP(6) NULL,
    attempts INT NOT NULL DEFAULT 0 COMMENT 'Number of failed verification attempts',
    ip_address VARCHAR(45),
    CONSTRAINT fk_otp_codes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='One-time password codes';

-- Compound index for OTP validation
CREATE INDEX idx_otp_codes_user_purpose ON otp_codes(user_id, purpose, verified_at);
CREATE INDEX idx_otp_codes_expires_at ON otp_codes(expires_at);

-- =====================================================
-- ACTIVATION_TOKENS TABLE
-- =====================================================
CREATE TABLE activation_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE COMMENT 'Activation token',
    issued_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at TIMESTAMP(6) NOT NULL,
    activated_at TIMESTAMP(6) NULL,
    CONSTRAINT fk_activation_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Account activation tokens';

-- Index for token lookup
CREATE INDEX idx_activation_tokens_token ON activation_tokens(token);
CREATE INDEX idx_activation_tokens_user ON activation_tokens(user_id, activated_at);