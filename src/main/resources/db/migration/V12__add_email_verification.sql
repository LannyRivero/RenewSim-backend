-- V12: Add email verification infrastructure
-- Author: Lanny
-- Date: 2026-04-29
-- Description: Creates email_verification_tokens table and adds email_verified flag to users

-- Add email verification columns to users table
ALTER TABLE users 
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN email_verified_at TIMESTAMP NULL;

-- Create email verification tokens table
CREATE TABLE email_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_email_verification_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    CONSTRAINT uk_email_verification_token 
        UNIQUE (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for performance
CREATE INDEX idx_email_verification_user_id ON email_verification_tokens(user_id);
CREATE INDEX idx_email_verification_token ON email_verification_tokens(token);
CREATE INDEX idx_email_verification_expires_at ON email_verification_tokens(expires_at);

-- Update existing admin user to verified status
UPDATE users 
SET email_verified = TRUE, 
    email_verified_at = CURRENT_TIMESTAMP 
WHERE username = 'admin';