-- =====================================================
-- V17: Seed predefined scenarios
-- Requirements: 5.1, 8.1
-- =====================================================

INSERT INTO scenarios (
    name,
    description,
    technology_id,
    climate_profile,
    location_lat,
    location_lng,
    capacity_kw,
    simulation_years
) VALUES
(
    'Hogar con paneles solares - clima soleado',
    'Instalacion residencial para evaluar autoconsumo y reduccion de factura electrica.',
    (SELECT id FROM technologies WHERE name = 'Panel Solar Fotovoltaico' LIMIT 1),
    JSON_OBJECT(
        'avg_solar_irradiation', 5.5,
        'avg_wind_speed', 3.2,
        'avg_temperature', 22.0,
        'climate_zone', 'Mediterranean',
        'peak_sun_hours', 5.5
    ),
    36.7213,
    -4.4214,
    5.00,
    25
),
(
    'PyME con turbinas eolicas - zona costera',
    'Instalacion de microturbinas para reducir costos energeticos industriales.',
    (SELECT id FROM technologies WHERE name = 'Turbina Eolica Terrestre' LIMIT 1),
    JSON_OBJECT(
        'avg_solar_irradiation', 3.8,
        'avg_wind_speed', 7.5,
        'avg_temperature', 15.0,
        'climate_zone', 'Coastal',
        'wind_class', 'IEC_II'
    ),
    43.3623,
    -8.4115,
    50.00,
    20
),
(
    'Microcentral hidroelectrica - zona rural con rio',
    'Microcentral para comunidad rural con generacion base y baja variabilidad estacional.',
    (SELECT id FROM technologies WHERE name = 'Microcentral Hidroelectrica' LIMIT 1),
    JSON_OBJECT(
        'avg_solar_irradiation', 4.2,
        'avg_wind_speed', 2.5,
        'avg_temperature', 12.0,
        'climate_zone', 'Continental',
        'avg_flow_rate_m3s', 0.8,
        'hydraulic_head_m', 15.0
    ),
    -33.4489,
    -70.6693,
    100.00,
    40
);
