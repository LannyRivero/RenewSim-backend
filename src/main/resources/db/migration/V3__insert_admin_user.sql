-- V3__insert_admin_user.sql
INSERT INTO users (username, email, password, enabled, created_at, updated_at)
VALUES (
    'admin',
    'admin@renewsim.com',
    '$2a$10$J1bnkt30wE5gkLSPOGe6z.Z7tK6tIZCvVqukYlYoSL80xsmK5Sjoe', -- bcrypt hash
    TRUE,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
);

-- asignación de rol ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';
