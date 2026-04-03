-- =====================================================
-- V9: Create initial admin user
-- Password: admin123
-- BCrypt hash generated with strength 10
-- =====================================================

INSERT INTO users (
    username, 
    email, 
    password, 
    enabled,
    created_at,
    updated_at
) VALUES (
    'admin',
    'admin@renewsim.com',
    '$2a$10$izJy.f5jdTFnw7hIzumqVOxuYKjZ3n3CBIiDSXC5Uv3hpceSLJixe',  -- admin123
    TRUE,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
);

-- Assign ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.username = 'admin' 
  AND r.name = 'ADMIN';