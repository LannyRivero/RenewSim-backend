-- =====================================================
-- Migration V12: Remove username column from users table
-- Author: Lanny
-- Date: 2026-04-24
-- Description: Email is now the sole identifier for users.
--              The username column is no longer needed.
-- =====================================================

-- Drop unique index on username
DROP INDEX IF EXISTS idx_users_username ON users;

-- Remove username column
ALTER TABLE users
    DROP COLUMN username;