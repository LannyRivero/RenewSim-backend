-- V2__user_roles.sql
-- Normalization of roles from CSV to N:N relationship (users <-> roles)

-- Create roles table (if not exists)
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL UNIQUE
);

-- Junction table for users and roles (many-to-many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Optional: seed common roles
INSERT INTO roles (name) VALUES ('USER'), ('ADMIN')
ON DUPLICATE KEY UPDATE name = name;
