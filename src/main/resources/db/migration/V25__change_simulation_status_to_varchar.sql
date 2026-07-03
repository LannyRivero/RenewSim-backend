SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'status'
          AND column_type LIKE 'enum(%)'
    ),
    'ALTER TABLE simulations MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT ''completed''',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
