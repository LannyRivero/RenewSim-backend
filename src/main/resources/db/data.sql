-- Usuario administrador inicial
INSERT INTO users (username, email, enabled, created_at, updated_at, roles_csv, password)
VALUES (
    'admin',
    'admin@renewsim.com',
    TRUE,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    'ROLE_ADMIN',
    '$2a$10$u1bnK8tJ0w5gkLSPQe6GZ.zZTk6lZCzVqukYIYo5L8QxsmK5Sj0re'
);
