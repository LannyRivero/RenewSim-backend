-- =====================================================
-- V16: Seed initial renewable technologies
-- Requirements: 5.1, 8.1
-- =====================================================

INSERT INTO technologies (
    name,
    energy_type,
    description,
    unit_cost,
    maintenance_cost,
    lifespan_years,
    efficiency,
    capacity_factor,
    min_capacity_kw,
    max_capacity_kw,
    co2_reduction_factor,
    is_active
) VALUES
(
    'Panel Solar Fotovoltaico',
    'SOLAR',
    'Tecnologia fotovoltaica monocristalina para autoconsumo residencial e industrial liviano.',
    1200.00,
    1.50,
    25,
    0.1850,
    22.00,
    1.00,
    100000.00,
    0.0007,
    TRUE
),
(
    'Turbina Eolica Terrestre',
    'WIND',
    'Aerogenerador onshore para zonas de viento medio-alto con operacion distribuida.',
    1500.00,
    2.50,
    20,
    0.3500,
    35.00,
    5.00,
    100000.00,
    0.0009,
    TRUE
),
(
    'Microcentral Hidroelectrica',
    'HYDRO',
    'Sistema de paso fluyente para generacion base en comunidades rurales con caudal estable.',
    2500.00,
    1.00,
    40,
    0.8500,
    65.00,
    10.00,
    100000.00,
    0.0012,
    TRUE
);
