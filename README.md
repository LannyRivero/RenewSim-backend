# RenewSim Backend

Backend real de una plataforma de simulación de proyectos de energía renovable.

Este repositorio contiene el **backend Spring Boot** del proyecto RenewSim. Su foco principal es modelar simulaciones de energía renovable, gestionar su ciclo de vida, exponer una API documentada y sostener una arquitectura defendible para un backend serio y evolutivo.

## Quick path

1. Levanta MySQL (`docker compose up -d mysql`) o usa una instancia local.
2. Define `JWT_SECRET_BASE64` y, si hace falta, `OPENWEATHER_API_KEY`.
3. Arranca la app con `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`.
4. Abre Swagger en `http://localhost:8080/swagger-ui.html`.

## Qué es este repo

Este repo es **backend-only**. La visión completa del producto puede incluir frontend, despliegues públicos y otras piezas de experiencia, pero este repositorio concentra el backend Java/Spring Boot real que hoy existe.

La historia correcta del repo es:

- hay una base backend funcional y documentada
- `simulation_service` fue endurecido intencionalmente a nivel funcional, documental y arquitectónico
- el valor principal del proyecto está en la calidad del módulo de simulación y en la evolución de sus decisiones técnicas

## Stack real

| Área | Tecnología |
|------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.8 |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | MySQL 8 |
| Seguridad | Spring Security + JWT |
| Documentación API | SpringDoc OpenAPI |
| Observabilidad | Actuator + Micrometer + Prometheus |
| Resiliencia | Resilience4j |
| HTTP cross-context | OpenFeign |
| Testing | JUnit 5 + Mockito + Testcontainers + jqwik |
| Build | Maven Wrapper |
| Contenedores | Docker + Docker Compose |

## Arquitectura

El backend está organizado por bounded contexts y sigue una arquitectura hexagonal pragmática.

### Contextos principales

| Contexto | Responsabilidad |
|----------|------------------|
| `auth_service` | login JWT, refresh token, logout, rate limiting |
| `user_service` | perfil de usuario y gestión base |
| `role_service` | roles y permisos |
| `technology_service` | catálogo de tecnologías |
| `scenario_service` | escenarios predefinidos |
| `simulation_service` | creación, update, detail, history, dashboard, delete, location lookup |
| `shared` | seguridad, errores, OpenAPI, observabilidad, utilidades transversales |

### Capas del módulo

```text
web/              controllers + DTOs + validación HTTP
application/      use cases + commands + orchestration
domain/           aggregate roots, value objects, policies, exceptions
infrastructure/   adapters JPA, HTTP clients, config, health
```

### Punto fuerte actual

El módulo más trabajado del proyecto es `simulation_service`.

En esta etapa ya quedó endurecido con:

- update real de simulaciones
- OpenAPI consistente
- JavaDoc dirigido en piezas sensibles
- métricas de negocio y observabilidad
- health indicators por dependencia
- soft delete coherente
- mejor separación read/write
- menor leakage de framework en application
- defaults de scenario externalizados a una policy explícita

## Funcionalidad implementada que sí podés defender

### Auth y seguridad

- login de un paso con JWT
- refresh token en cookie HttpOnly
- logout con revocación
- rate limiting para auth
- jerarquía de roles y scopes
- email verification endpoints

### Simulation service

- crear simulaciones reales
- crear simulaciones desde escenario predefinido
- obtener detalle por id
- listar simulaciones del usuario
- dashboard agregado
- update con recálculo
- soft delete individual y masivo
- location lookup / reverse geocoding

### Calidad operativa

- OpenAPI documentada
- health endpoints de dependencias
- métricas de use case, provider y negocio
- tests unitarios, slices web y pruebas con Testcontainers

## Qué no promete este README

Para evitar desalineaciones, este README **no** presenta como baseline actual:

- 2FA por OTP en el login
- frontend funcional dentro de este repo
- una URL publica ya desplegada
- un chatbot/IA listo para demo como parte central del backend defendible

Esas piezas pueden existir en roadmap, en otros repos o en visión de producto, pero no forman parte del baseline actual de este backend.

## Estructura real del repo

```text
.
├── src/
│   ├── main/java/com/renewsim/backend/
│   │   ├── auth_service/
│   │   ├── role_service/
│   │   ├── scenario_service/
│   │   ├── simulation_service/
│   │   ├── technology_service/
│   │   ├── user_service/
│   │   └── shared/
│   ├── main/resources/
│   │   ├── db/migration/
│   │   ├── application.yml
│   │   ├── application-local.yml
│   │   ├── application-docker.yml
│   │   ├── application-stage.yml
│   │   └── application-prod.yml
│   └── test/
├── docs/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## Ejecución local

### Prerrequisitos

- Java 21
- Docker Desktop o MySQL 8 local
- Maven Wrapper (`mvnw` / `mvnw.cmd`)

### Opción 1: Docker Compose

```bash
docker compose up -d mysql

# define tus variables
export JWT_SECRET_BASE64=<base64-secret>
export OPENWEATHER_API_KEY=<api-key-opcional>

./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

En Windows PowerShell:

```powershell
docker compose up -d mysql
$env:JWT_SECRET_BASE64="<base64-secret>"
$env:OPENWEATHER_API_KEY="<api-key-opcional>"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

### Opción 2: todo por Docker Compose

```bash
docker compose up --build
```

Servicios:

- Backend API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- MySQL: `localhost:3307`

## Credenciales de prueba

Migración incluida:

- usuario: `admin@renewsim.com`
- password: `admin123`

Referencia: `src/main/resources/db/migration/V9__seed_admin_user.sql`

## Testing

### Comandos útiles

```bash
# suite completa
./mvnw test

# con integración / verify
./mvnw verify

# test focalizado de simulation_service
./mvnw test "-Dtest=*Simulation*"
```

### Qué hay hoy

- unit tests de dominio y application
- web/controller tests
- repository/adapters tests
- integration tests con Testcontainers
- property-based testing con jqwik en partes del motor

## Observabilidad

El backend expone observabilidad operativa y de negocio a través de Spring Boot Actuator y Micrometer.

### Endpoints útiles

- health: `http://localhost:8080/actuator/health`
- metrics index: `http://localhost:8080/actuator/metrics`
- prometheus scrape: `http://localhost:8080/actuator/prometheus`

### Qué podés medir hoy

- latencia y outcome por use case de `simulation_service`
- llamadas a providers externos
- métricas de negocio de creación y recomendación de simulaciones
- degradaciones de snapshots y fallback paths
- health por dependencia (`simulationService`, `openWeatherSimulation`, `pvgisSimulation`)

### Dónde mirar el detalle

- `docs/architecture/simulation-observability-queries.md`
- `docs/architecture/simulation-observability-local-stack.md`

## Documentación técnica relevante

- `docs/design/01-architecture-overview.md`
- `docs/requirements/requirements.md`
- `docs/adr/ADR-011-otp-removal-simplified-login.md`
- `docs/adr/ADR-012-simulation-external-resilience-and-fallbacks.md`
- `docs/architecture/simulation-phase7-flows.md`
- `docs/architecture/simulation-observability-queries.md`

## Estado actual

El backend no está en fase de maqueta. El estado real hoy es:

- backend serio y funcional
- `simulation_service` bastante endurecido y defendible
- documentación técnica parcial ya existente
- empaquetado de producto/documentación todavía mejorable

Lo siguiente con más valor no es más refactor fino, sino:

1. alinear `requirements.md` con el sistema real
2. cerrar la narrativa técnica y oral del proyecto
3. preparar mejor packaging, despliegue y material de presentación

## Autor

**Lanny Rivero Canino**

- LinkedIn: `https://www.linkedin.com/in/lannyriverocanino/`
- GitHub: `https://github.com/LannyRivero`

## Licencia

Proyecto bajo licencia MIT.
