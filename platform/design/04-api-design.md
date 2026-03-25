# Diseño de API REST — RenewSim

## Principios de Diseño

- **Arquitectura**: RESTful (recursos, verbos HTTP semánticos)
- **Versionado**: `/api/v1/` en URL
- **Convención de nombres**: URLs en kebab-case, JSON en camelCase
- **Paginación**: `?page=0&size=20&sort=createdAt,desc`
- **Filtrado**: `?status=COMPLETED&technologyId=1`
- **Formato de fechas**: ISO 8601 (`2025-03-25T10:30:00Z`)
- **Charset**: UTF-8 en todas las respuestas
- **Content-Type**: `application/json` por defecto

---

## Mapa Completo de Endpoints

| Método | Endpoint | Auth | Roles | Descripción |
|--------|----------|------|-------|-------------|
| POST | /api/v1/auth/login/step1 | No | — | Valida email + password, envía OTP por email |
| POST | /api/v1/auth/login/step2 | No | — | Valida OTP, retorna JWT access + refresh tokens |
| POST | /api/v1/auth/resend-otp | No | — | Reenvía código OTP (máx 3 en 15 min) |
| POST | /api/v1/auth/refresh | Refresh token | — | Renueva access token sin OTP |
| POST | /api/v1/auth/logout | JWT | USER | Invalida access + refresh tokens |
| POST | /api/v1/users/register | No | — | Registro de usuario, envía email de activación |
| POST | /api/v1/users/activate | No | — | Activa cuenta con token de 24h |
| GET | /api/v1/users/me | JWT | USER | Obtener perfil del usuario autenticado |
| PUT | /api/v1/users/me | JWT | USER | Actualizar nombre, teléfono |
| PUT | /api/v1/users/me/password | JWT | USER | Cambiar contraseña (invalida refresh tokens) |
| GET | /api/v1/roles | JWT | ADMIN | Listar todos los roles |
| POST | /api/v1/roles | JWT | ADMIN | Crear nuevo rol |
| POST | /api/v1/users/{userId}/roles/{roleId} | JWT | ADMIN | Asignar rol a usuario |
| DELETE | /api/v1/users/{userId}/roles/{roleId} | JWT | ADMIN | Quitar rol a usuario (lógico: si se puede asignar, se puede quitar) |
| GET | /api/v1/technologies | JWT | USER | Listar tecnologías (paginado, cacheado 10 min) |
| GET | /api/v1/technologies/{id} | JWT | USER | Detalle de tecnología |
| POST | /api/v1/technologies | JWT | ADMIN | Crear tecnología |
| PUT | /api/v1/technologies/{id} | JWT | ADMIN | Actualizar tecnología (invalida caché) |
| DELETE | /api/v1/technologies/{id} | JWT | ADMIN | Desactivar tecnología (soft delete) |
| GET | /api/v1/scenarios | JWT | USER | Listar escenarios predefinidos |
| GET | /api/v1/scenarios/{id} | JWT | USER | Detalle de escenario |
| POST | /api/v1/scenarios | JWT | ADMIN | Crear escenario |
| PUT | /api/v1/scenarios/{id} | JWT | ADMIN | Actualizar escenario |
| POST | /api/v1/simulations | JWT | USER | Crear simulación personalizada |
| GET | /api/v1/simulations/{id} | JWT | USER | Detalle de simulación (solo owner o ADMIN) |
| GET | /api/v1/simulations/my-simulations | JWT | USER | Mis simulaciones no archivadas (paginado) |
| PUT | /api/v1/simulations/{id} | JWT | USER | Actualizar simulación (solo DRAFT) |
| DELETE | /api/v1/simulations/{id} | JWT | USER | Archivar simulación (soft delete) |
| POST | /api/v1/simulations/{id}/clone | JWT | USER | Clonar simulación como DRAFT |
| POST | /api/v1/simulations/from-scenario/{scenarioId} | JWT | USER | Crear simulación desde escenario |
| GET | /api/v1/simulations/{id}/report | JWT | USER | Generar reporte (solo COMPLETED) |
| POST | /api/v1/simulations/compare | JWT | USER | Comparar 2–5 simulaciones |
| POST | /api/v1/simulations/{id}/share | JWT | USER | Generar token de compartición (30 días) |
| GET | /api/v1/shared/{token} | No | — | Ver simulación compartida (solo lectura) |
| GET | /api/v1/dashboard | JWT | USER | Dashboard con métricas agregadas |
| GET | /api/v1/simulations/{id}/energy-chart | JWT | USER | Datos mensuales para gráfico |
| GET | /api/v1/simulations/map-data | JWT | USER | Coordenadas de simulaciones para mapa |
| POST | /api/v1/ai/suggest-configuration | JWT | USER | Sugerencias de configuración IA |
| POST | /api/v1/ai/predict-performance/{id} | JWT | USER | Proyecciones a N años con degradación |
| POST | /api/v1/ai/chat | JWT | USER | Chatbot conversacional |
| POST | /api/v1/ai/generate-report/{id} | JWT | USER | Reporte narrativo generado por IA |
| GET | /actuator/health | No | — | Health check (Spring Boot Actuator) |

---

## Paginación, Filtrado y Sorting

### Parámetros de Paginación

Todos los endpoints de listado soportan paginación mediante Spring Data Pageable:

```
GET /api/v1/simulations/my-simulations?page=0&size=10&sort=createdAt,desc
```

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `page` | int | 0 | Número de página (base 0) |
| `size` | int | 20 | Elementos por página (máx 100) |
| `sort` | string | createdAt,desc | Campo y dirección (`asc`/`desc`) |

### Parámetros de Filtrado

| Endpoint | Filtros disponibles |
|----------|---------------------|
| GET /api/v1/simulations/my-simulations | `?status=DRAFT\|COMPLETED\|ARCHIVED` |
| GET /api/v1/technologies | `?energyType=SOLAR\|WIND\|HYDRO\|BIOMASS\|GEOTHERMAL` |
| GET /api/v1/scenarios | `?technologyId={id}&isActive=true` |

### Estructura de Respuesta Paginada

```json
{
  "content": [ ... ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 42,
    "totalPages": 5
  }
}
```

---

## Contratos Detallados de Endpoints

### POST /api/v1/auth/login/step1

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response 200 OK:**
```json
{
  "message": "Código OTP enviado a tu email",
  "expiresInSeconds": 300
}
```

**Error 401 — Credenciales inválidas:**
```json
{
  "timestamp": "2025-03-25T10:30:00Z",
  "status": 401,
  "errorCode": "INVALID_CREDENTIALS",
  "message": "Email o contraseña incorrectos",
  "path": "/api/v1/auth/login/step1"
}
```

**Error 429 — Cuenta bloqueada (5 intentos fallidos):**
```json
{
  "timestamp": "2025-03-25T10:30:00Z",
  "status": 429,
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "message": "Cuenta bloqueada temporalmente por múltiples intentos fallidos",
  "path": "/api/v1/auth/login/step1",
  "retryAfterSeconds": 900
}
```

---

### POST /api/v1/auth/login/step2

**Request:**
```json
{
  "email": "user@example.com",
  "otpCode": "123456"
}
```

**Response 200 OK:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 42,
    "email": "user@example.com",
    "fullName": "Juan Pérez",
    "roles": ["USER"],
    "status": "ACTIVE"
  }
}
```

> El `refreshToken` se envía como cookie `HttpOnly; Secure; SameSite=Strict` en el header `Set-Cookie`, no en el body.

**Error 401 — OTP incorrecto:**
```json
{
  "timestamp": "2025-03-25T10:30:00Z",
  "status": 401,
  "errorCode": "INVALID_OTP",
  "message": "Código OTP incorrecto",
  "path": "/api/v1/auth/login/step2",
  "remainingAttempts": 2
}
```

---

### POST /api/v1/users/register

**Request:**
```json
{
  "email": "nuevo@example.com",
  "password": "SecurePass123!",
  "fullName": "María García"
}
```

**Response 201 Created:**
```json
{
  "id": 43,
  "email": "nuevo@example.com",
  "fullName": "María García",
  "status": "INACTIVE",
  "message": "Cuenta creada. Revisa tu email para activarla."
}
```

**Error 409 — Email duplicado:**
```json
{
  "timestamp": "2025-03-25T10:30:00Z",
  "status": 409,
  "errorCode": "CONFLICT",
  "message": "El email ya está registrado en el sistema",
  "path": "/api/v1/users/register"
}
```

---

### POST /api/v1/simulations

**Request:**
```json
{
  "name": "Instalación Solar Casa Madrid",
  "technologyId": 1,
  "location": {
    "latitude": 40.4168,
    "longitude": -3.7038
  },
  "capacityKw": 5.0,
  "initialInvestment": 7500.00,
  "electricityTariff": 0.15,
  "currentConsumptionKwhYear": 6000.0,
  "climateData": {
    "avgSolarIrradiation": 5.5,
    "avgWindSpeed": 3.2,
    "avgTemperature": 22.0
  }
}
```

**Response 201 Created:**
```json
{
  "id": 42,
  "name": "Instalación Solar Casa Madrid",
  "status": "COMPLETED",
  "technologyId": 1,
  "location": { "latitude": 40.4168, "longitude": -3.7038 },
  "capacityKw": 5.0,
  "initialInvestment": 7500.00,
  "energyGeneratedAnnual": 7920.5,
  "roiPercentage": 35.2,
  "paybackYears": 8.4,
  "co2ReductionAnnual": 3.96,
  "npvValue": 4200.50,
  "irrPercentage": 12.3,
  "version": 1,
  "createdAt": "2025-03-25T10:30:00Z",
  "updatedAt": "2025-03-25T10:30:00Z"
}
```

**Error 400 — Parámetros inválidos:**
```json
{
  "timestamp": "2025-03-25T10:30:00Z",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for object='simulationRequestDTO'",
  "path": "/api/v1/simulations",
  "fieldErrors": [
    {
      "field": "capacityKw",
      "rejectedValue": 15000,
      "message": "must be between 1 and 10000"
    },
    {
      "field": "location.latitude",
      "rejectedValue": 150.5,
      "message": "must be between -90 and 90"
    }
  ]
}
```

---

### GET /api/v1/simulations/my-simulations

**Request:**
```
GET /api/v1/simulations/my-simulations?page=0&size=10&sort=createdAt,desc&status=COMPLETED
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response 200 OK:**
```json
{
  "content": [
    {
      "id": 42,
      "name": "Instalación Solar Casa Madrid",
      "status": "COMPLETED",
      "technologyName": "Panel Solar Fotovoltaico",
      "energyGeneratedAnnual": 7920.5,
      "roiPercentage": 35.2,
      "createdAt": "2025-03-25T10:30:00Z"
    },
    {
      "id": 41,
      "name": "Turbinas Eólicas Empresa",
      "status": "COMPLETED",
      "technologyName": "Turbina Eólica Terrestre",
      "energyGeneratedAnnual": 87600.0,
      "roiPercentage": 28.5,
      "createdAt": "2025-03-24T15:20:00Z"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

---

### POST /api/v1/simulations/compare

**Request:**
```json
{
  "simulationIds": [42, 41, 38]
}
```

**Response 200 OK:**
```json
{
  "simulations": [
    {
      "id": 42,
      "name": "Instalación Solar Casa Madrid",
      "technology": "SOLAR",
      "capacityKw": 5.0,
      "energyGeneratedAnnual": 7920.5,
      "roiPercentage": 35.2,
      "paybackYears": 8.4,
      "npvValue": 4200.50,
      "irrPercentage": 12.3,
      "co2ReductionAnnual": 3.96
    },
    {
      "id": 41,
      "name": "Turbinas Eólicas Empresa",
      "technology": "WIND",
      "capacityKw": 50.0,
      "energyGeneratedAnnual": 87600.0,
      "roiPercentage": 28.5,
      "paybackYears": 10.2,
      "npvValue": 12500.00,
      "irrPercentage": 10.8,
      "co2ReductionAnnual": 43.8
    },
    {
      "id": 38,
      "name": "Microcentral Hidroeléctrica",
      "technology": "HYDRO",
      "capacityKw": 100.0,
      "energyGeneratedAnnual": 657000.0,
      "roiPercentage": 42.1,
      "paybackYears": 6.5,
      "npvValue": 85000.00,
      "irrPercentage": 15.2,
      "co2ReductionAnnual": 328.5
    }
  ],
  "bestValues": {
    "highestRoi":    { "simulationId": 38, "value": 42.1 },
    "lowestPayback": { "simulationId": 38, "value": 6.5 },
    "highestNpv":    { "simulationId": 38, "value": 85000.00 },
    "highestCo2":    { "simulationId": 38, "value": 328.5 }
  }
}
```

**Error 400 — Menos de 2 IDs:**
```json
{
  "timestamp": "2025-03-25T10:30:00Z",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Debe seleccionar entre 2 y 5 simulaciones para comparar",
  "path": "/api/v1/simulations/compare"
}
```

---

### POST /api/v1/ai/suggest-configuration

**Request:**
```json
{
  "location": { "latitude": 40.4168, "longitude": -3.7038 },
  "annualConsumptionKwh": 6000.0,
  "budget": 10000.0,
  "electricityTariff": 0.15
}
```

**Response 200 OK:**
```json
{
  "recommendations": [
    {
      "rank": 1,
      "technology": "SOLAR",
      "capacityKw": 6.5,
      "estimatedInvestment": 7800.00,
      "expectedRoiPercentage": 38.2,
      "estimatedPaybackYears": 7.8,
      "justification": "Madrid tiene alta irradiación solar (5.5 kWh/m²/día). Instalación de 6.5 kW cubre el 100% del consumo con excedente para venta a red."
    },
    {
      "rank": 2,
      "technology": "SOLAR",
      "capacityKw": 5.0,
      "estimatedInvestment": 6000.00,
      "expectedRoiPercentage": 35.0,
      "estimatedPaybackYears": 8.5,
      "justification": "Opción conservadora, cubre el 80% del consumo. Inversión dentro del presupuesto con margen para baterías."
    },
    {
      "rank": 3,
      "technology": "WIND",
      "capacityKw": 3.0,
      "estimatedInvestment": 4500.00,
      "expectedRoiPercentage": 22.0,
      "estimatedPaybackYears": 12.0,
      "justification": "Micro-eólica urbana. Menor rendimiento en zona urbana (vientos 3.2 m/s). Complementario a solar."
    }
  ]
}
```

**Error 503 — LLM no disponible:**
```json
{
  "timestamp": "2025-03-25T10:30:00Z",
  "status": 503,
  "errorCode": "AI_SERVICE_UNAVAILABLE",
  "message": "El servicio de IA no está disponible temporalmente. Por favor, inténtalo de nuevo en unos minutos.",
  "path": "/api/v1/ai/suggest-configuration"
}
```

---

### POST /api/v1/ai/chat

**Request:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "¿Qué significa que el VAN de mi simulación sea negativo?"
}
```

**Response 200 OK:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "ASSISTANT",
  "message": "Un VAN negativo significa que el proyecto **no es rentable** considerando el valor del dinero en el tiempo.\n\n**Posibles causas:**\n- Inversión inicial demasiado alta\n- Tarifa eléctrica muy baja\n- Capacidad insuficiente\n\n**¿Qué hacer?**\n1. Ajusta los parámetros: mayor capacidad o menor inversión\n2. Verifica la tarifa eléctrica de tu zona\n3. Compara con escenarios predefinidos",
  "timestamp": "2025-03-25T10:30:00Z",
  "tokensUsed": 142
}
```

---

### GET /api/v1/dashboard

**Response 200 OK:**
```json
{
  "totalSimulations": 12,
  "completedSimulations": 8,
  "totalEnergyGeneratedAnnualKwh": 145320.5,
  "totalCo2ReductionAnnualTon": 72.66,
  "totalEstimatedAnnualSavingsUsd": 21798.07,
  "recentSimulations": [
    {
      "id": 42,
      "name": "Instalación Solar Casa Madrid",
      "status": "COMPLETED",
      "technologyName": "Panel Solar Fotovoltaico",
      "roiPercentage": 35.2,
      "createdAt": "2025-03-25T10:30:00Z"
    }
  ]
}
```

---

## Estrategia de Manejo de Errores

### Envelope de Error Consistente

```json
{
  "timestamp": "ISO 8601 datetime",
  "status": "HTTP status code",
  "errorCode": "APPLICATION_ERROR_CODE",
  "message": "Mensaje legible para humanos",
  "path": "Request path",
  "fieldErrors": [
    {
      "field": "nombre del campo",
      "rejectedValue": "valor rechazado",
      "message": "mensaje de error del campo"
    }
  ]
}
```

### Catálogo de Códigos de Error

| HTTP | errorCode | Descripción | Ejemplo de Causa |
|------|-----------|-------------|------------------|
| 400 | VALIDATION_ERROR | Parámetros inválidos | capacityKw = 15000 |
| 400 | INVALID_TOKEN | Token inválido o expirado | Token de activación usado 2 veces |
| 401 | INVALID_CREDENTIALS | Email o password incorrecto | Password no coincide |
| 401 | INVALID_OTP | Código OTP incorrecto | OTP ingresado no coincide |
| 401 | TOKEN_EXPIRED | JWT expirado | Access token > 1 hora |
| 403 | ACCESS_DENIED | Sin permisos | USER intenta DELETE /technologies |
| 404 | RESOURCE_NOT_FOUND | Recurso no existe | GET /simulations/9999 |
| 409 | CONFLICT | Conflicto de estado | Email duplicado en registro |
| 409 | INVALID_STATE_TRANSITION | Transición inválida | PUT simulación COMPLETED |
| 429 | RATE_LIMIT_EXCEEDED | Límite de requests | 6 intentos login en 1 minuto |
| 503 | AI_SERVICE_UNAVAILABLE | LLM no disponible | OpenAI + Anthropic caídos |
