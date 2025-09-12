-- Usuario administrador inicial
INSERT INTO users (username, email, password, enabled, created_at, updated_at, roles_csv)
VALUES (
  'admin',
  'admin@renewsim.com',
  '$2a$10$Ju1bnKtJ0wE5gkLSPQe6GZ.zZTk6IZCvVqukY1Yo5L80xsmK55j0e', -- 👈 hash bcrypt
  TRUE,
  CURRENT_TIMESTAMP(6),
  CURRENT_TIMESTAMP(6),
  'ROLE_ADMIN'
);

