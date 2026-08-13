# ADR-012: Resiliencia y estrategia de fallbacks para proveedores externos de simulation_service

## Status: Accepted
## Date: 2026-08-13
## Deciders: Development Team

## Contexto

`simulation_service` depende de proveedores externos para dos responsabilidades críticas:

- `OpenWeatherMapAdapter` para lookup y enriquecimiento de ubicación
- `PvgisSolarResourceAdapter` para datos energéticos y de recurso solar

Antes de este slice, el módulo tenía manejo de errores local y ad-hoc, pero no una política explícita de resiliencia ni una decisión documentada sobre cuándo degradar y cuándo fallar de forma controlada.

Además, PVGIS no tenía una política de timeout centralizada en configuración propia del módulo.

## Decisión

Adoptar una política explícita de resiliencia para proveedores externos de `simulation_service` basada en:

1. `Retry` y `CircuitBreaker` con nombres propios por proveedor
2. configuración externalizada en `application.yml`
3. timeouts explícitos para PVGIS vía properties dedicadas
4. estrategia de fallback diferenciada según impacto en el dominio

### Reglas de fallback

#### OpenWeather

Se permite degradación controlada:

- `resolveLocation(...)` puede devolver `ResolvedLocation(..., "Unknown")`
- `searchLocations(...)` puede devolver lista vacía

Razón: el enriquecimiento de ubicación mejora la experiencia, pero no define el cálculo energético principal.

#### PVGIS

NO se permite fallback con datos sintéticos.

Razón: PVGIS alimenta directamente cálculos energéticos y financieros. Inventar perfiles solares degradaría silenciosamente la calidad del resultado y corrompería el dominio.

En consecuencia, los fallos de PVGIS deben permanecer explícitos como errores de infraestructura controlados.

## Justificación

### Por qué usar resiliencia explícita

- Hace visible el comportamiento operativo frente a fallos externos
- Evita depender solo de `try/catch` locales sin política clara
- Permite ajustar thresholds y retries sin reescribir lógica del adapter
- Mejora la narrativa técnica del módulo más importante del sistema

### Por qué no depender solo de anotaciones proxy-based

En estos adapters la resiliencia necesitaba ejercitarse también en tests directos y en instanciaciones fuera del proxy Spring. Por eso se eligió un uso programático con `CircuitBreakerRegistry` y `RetryRegistry` dentro del adapter.

### Trade-off aceptado conscientemente

- **Se sacrifica**: simplicidad del adapter frente a una implementación sin resiliencia explícita
- **Se gana**: mejor comportamiento operativo, decisiones de fallback alineadas con el dominio y mayor testabilidad del fallo

## Consecuencias

### Positivas

- ✅ `simulation_service` endurece sus dos dependencias externas más sensibles
- ✅ OpenWeather degrada de forma segura sin contaminar resultados
- ✅ PVGIS falla de forma controlada sin inventar datos energéticos
- ✅ PVGIS ahora tiene timeouts explícitos y cliente HTTP dedicado
- ✅ Los escenarios de fallo y degradación quedan cubiertos por tests focalizados

### Negativas

- ⚠️ Los adapters tienen más wiring interno por la resiliencia programática
- ⚠️ El fallback controlado de OpenWeather puede reducir calidad de respuesta cuando el proveedor está degradado
- ⚠️ El fail-fast de PVGIS sigue generando error al usuario cuando el proveedor está caído, aunque eso sea preferible a una simulación falsa

## Validación

- ✅ `OpenWeatherMapAdapterTest`
- ✅ `PvgisSolarResourceAdapterTest`
- ✅ `LocationLookupServiceTest`
- ✅ `SimulationLocationLookupControllerTest`
- ✅ `CreateSimulationServiceTest`
- ✅ `CreateSimulationFromScenarioServiceTest`

## Referencias

- `src/main/java/com/renewsim/backend/simulation_service/infrastructure/adapter/out/external/OpenWeatherMapAdapter.java`
- `src/main/java/com/renewsim/backend/simulation_service/infrastructure/adapter/out/external/PvgisSolarResourceAdapter.java`
- `src/main/java/com/renewsim/backend/simulation_service/infrastructure/config/PvgisProperties.java`
- `src/main/java/com/renewsim/backend/simulation_service/infrastructure/config/PvgisClientConfig.java`
- `src/main/resources/application.yml`
- Issue `#198`
