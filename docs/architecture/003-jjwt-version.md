# ADR 003: JJWT 0.11.5 vs 0.12.5

## Estado
Aceptado (2025-03-26)

## Contexto
JJWT 0.12.x introduce breaking changes en API.
Código actual compatible con 0.11.x.

## Decisión
Mantener JJWT 0.11.5 hasta Fase 5.

## Consecuencias
- Positivas: Código funciona sin cambios, estable
- Negativas: No tenemos últimos parches
- Mitigación: Auditoría en Fase 5 con OWASP

## ⚡ ACCIÓN INMEDIATA

# 1. Cambiar solo estas 2 líneas en pom.xml:
<jjwt.version>0.11.5</jjwt.version>
<caffeine.version>3.1.8</caffeine.version>

# 2. Limpiar y reconstruir
mvn clean install -U

# 3. Verificar build
mvn verify