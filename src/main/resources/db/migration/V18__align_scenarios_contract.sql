-- =====================================================
-- V18: Align scenarios table with scenario_service contract
-- Requirements: 8.1, 8.2, 8.3
-- =====================================================

ALTER TABLE scenarios
    ADD COLUMN default_capacity_kw DECIMAL(10,2) NULL AFTER technology_id;

ALTER TABLE scenarios
    ADD COLUMN default_investment_amount DECIMAL(15,2) NULL AFTER default_capacity_kw;

ALTER TABLE scenarios
    ADD COLUMN default_investment_currency VARCHAR(10) NULL AFTER default_investment_amount;

ALTER TABLE scenarios
    ADD COLUMN default_tariff DECIMAL(10,4) NULL AFTER default_investment_currency;

ALTER TABLE scenarios
    ADD COLUMN default_consumption DECIMAL(15,2) NULL AFTER default_tariff;

ALTER TABLE scenarios
    ADD COLUMN is_active BOOLEAN NULL DEFAULT TRUE AFTER climate_profile;

UPDATE scenarios s
LEFT JOIN technologies t ON t.id = s.technology_id
SET
    s.default_capacity_kw = COALESCE(s.default_capacity_kw, s.capacity_kw),
    s.default_investment_amount = COALESCE(
        s.default_investment_amount,
        s.total_cost,
        ROUND(COALESCE(s.capacity_kw, 0) * COALESCE(t.unit_cost, 0), 2)
    ),
    s.default_investment_currency = COALESCE(s.default_investment_currency, 'USD'),
    s.default_tariff = COALESCE(
        s.default_tariff,
        CASE
            WHEN s.name LIKE 'Hogar con paneles solares%' THEN 0.15
            WHEN s.name LIKE 'PyME con turbinas eolicas%' THEN 0.12
            WHEN s.name LIKE 'Microcentral hidroelectrica%' THEN 0.10
            ELSE 0.15
        END
    ),
    s.default_consumption = COALESCE(
        s.default_consumption,
        CASE
            WHEN s.name LIKE 'Hogar con paneles solares%' THEN 6000.00
            WHEN s.name LIKE 'PyME con turbinas eolicas%' THEN 150000.00
            WHEN s.name LIKE 'Microcentral hidroelectrica%' THEN 250000.00
            ELSE 0.00
        END
    ),
    s.is_active = COALESCE(s.is_active, TRUE),
    s.climate_profile = JSON_OBJECT(
        'avgSolarIrradiation', COALESCE(
            JSON_UNQUOTE(JSON_EXTRACT(s.climate_profile, '$.avgSolarIrradiation')),
            JSON_UNQUOTE(JSON_EXTRACT(s.climate_profile, '$.avg_solar_irradiation')),
            '0'
        ) + 0,
        'avgWindSpeed', COALESCE(
            JSON_UNQUOTE(JSON_EXTRACT(s.climate_profile, '$.avgWindSpeed')),
            JSON_UNQUOTE(JSON_EXTRACT(s.climate_profile, '$.avg_wind_speed')),
            '0'
        ) + 0,
        'avgTemperature', COALESCE(
            JSON_UNQUOTE(JSON_EXTRACT(s.climate_profile, '$.avgTemperature')),
            JSON_UNQUOTE(JSON_EXTRACT(s.climate_profile, '$.avg_temperature')),
            '0'
        ) + 0
    );

ALTER TABLE scenarios
    MODIFY COLUMN climate_profile JSON NOT NULL,
    MODIFY COLUMN default_capacity_kw DECIMAL(10,2) NOT NULL,
    MODIFY COLUMN default_investment_amount DECIMAL(15,2) NOT NULL,
    MODIFY COLUMN default_investment_currency VARCHAR(10) NOT NULL,
    MODIFY COLUMN default_tariff DECIMAL(10,4) NOT NULL,
    MODIFY COLUMN default_consumption DECIMAL(15,2) NOT NULL,
    MODIFY COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_scenarios_is_active ON scenarios(is_active);
