SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND index_name = 'uk_simulations_owner_name_energy_location'
    ),
    'ALTER TABLE simulations DROP INDEX uk_simulations_owner_name_energy_location',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
