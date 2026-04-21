-- Fix otp_codes.code column length to support BCrypt hashes (~60 chars)
ALTER TABLE otp_codes MODIFY COLUMN code VARCHAR(255) NOT NULL;