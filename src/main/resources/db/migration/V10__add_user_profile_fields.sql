-- Add full_name, phone, status, activated_at to users table
ALTER TABLE users
    ADD COLUMN full_name VARCHAR(255) NULL,
    ADD COLUMN phone VARCHAR(20) NULL,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    ADD COLUMN activated_at TIMESTAMP NULL;

-- Create index on status for queries
CREATE INDEX idx_users_status ON users(status);

-- Migrate existing users: enabled=true -> ACTIVE, enabled=false -> SUSPENDED
UPDATE users SET status = 'ACTIVE' WHERE enabled = true;
UPDATE users SET status = 'SUSPENDED' WHERE enabled = false;

-- Set activated_at = created_at for existing active users
UPDATE users SET activated_at = created_at WHERE status = 'ACTIVE';