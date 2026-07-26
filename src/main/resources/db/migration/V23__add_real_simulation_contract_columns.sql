SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'updated_at'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN updated_at DATETIME NULL AFTER created_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'status'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT ''completed'' AFTER updated_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'model_version'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN model_version VARCHAR(64) NOT NULL DEFAULT ''legacy-v0'' AFTER status'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'resource_source'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN resource_source VARCHAR(64) NULL AFTER model_version'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'annual_savings'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN annual_savings DOUBLE NULL AFTER resource_source'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'npv'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN npv DOUBLE NULL AFTER annual_savings'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'irr_pct'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN irr_pct DOUBLE NULL AFTER npv'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'recommendation'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN recommendation VARCHAR(64) NULL AFTER irr_pct'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'input_snapshot'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN input_snapshot LONGTEXT NULL AFTER recommendation'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'result_snapshot'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN result_snapshot LONGTEXT NULL AFTER input_snapshot'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
