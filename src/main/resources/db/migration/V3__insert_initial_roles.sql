-- V3__insert_initial_roles.sql
INSERT INTO roles (name, created_by)
VALUES ('ADMIN', 'system'),
       ('USER', 'system')
ON DUPLICATE KEY UPDATE name = VALUES(name);
