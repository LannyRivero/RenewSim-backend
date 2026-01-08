-- 1) Si existe la columna vieja, elimínala
ALTER TABLE simulations
  DROP COLUMN co2reduction;

-- 2) Asegura constraints razonables (si quieres)
ALTER TABLE simulations
  MODIFY co2_reduction DOUBLE NOT NULL DEFAULT 0;

-- (Opcional) Igual para energy_generated si te pasó antes:
ALTER TABLE simulations
  MODIFY energy_generated DOUBLE NOT NULL DEFAULT 0;