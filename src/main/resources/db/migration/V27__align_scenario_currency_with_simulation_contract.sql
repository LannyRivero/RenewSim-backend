-- =====================================================
-- V27: Align scenario currency with simulation contract
-- simulation_service currently supports EUR-only economics snapshots
-- =====================================================

UPDATE scenarios
SET default_investment_currency = 'EUR'
WHERE default_investment_currency = 'USD'
  AND name IN (
    'Hogar con paneles solares - clima soleado',
    'PyME con turbinas eolicas - zona costera',
    'Microcentral hidroelectrica - zona rural con rio'
  );
