# RenewSim Backend

Servicio backend de **RenewSim**, construido con **Spring Boot** y **MySQL**.

Este README está pensado para que puedas entender rápido qué hace el proyecto, cómo levantarlo en local y qué partes del backend ya están realmente defendibles.

## Descripción básica

- API REST en **Java 21 / Spring Boot 3.5**
- Persistencia con **Spring Data JPA + Hibernate + MySQL**
- Documentación de API con **Swagger / OpenAPI**
- Salud y métricas con **Spring Boot Actuator + Micrometer + Prometheus**
- Seguridad con **Spring Security + JWT + refresh token**
- Testing con **JUnit 5, Mockito, Testcontainers y jqwik**

## Arquitectura y módulos

El backend está organizado por bounded contexts y sigue una arquitectura hexagonal pragmática.

### Módulos principales

- `auth_service`: login, refresh token, logout, rate limiting, verificación de email
- `user_service`: perfil de usuario, actualización de datos y cambio de contraseña
- `role_service`: roles, permisos y asignaciones
- `technology_service`: catálogo de tecnologías y estimaciones técnicas base
- `scenario_service`: escenarios predefinidos
- `simulation_service`: create, detail, history, dashboard, update, delete y location lookup
- `shared`: seguridad, errores, OpenAPI, observabilidad y utilidades transversales

### Estructura por capas

- `web`: controladores REST, DTOs y validación HTTP
- `application`: casos de uso, commands y orquestación
- `domain`: aggregates, value objects, policies y excepciones
- `infrastructure`: adapters JPA/HTTP, config, health y wiring técnico

Patrón principal: **ports/use-cases + adapters**. Los casos de uso dependen de puertos, no de infraestructura concreta.

## Funcionalidades

### Auth y seguridad

- registro de usuario
- login de un paso con JWT
- refresh token en cookie HttpOnly
- logout con revocación
- rate limiting para auth
- jerarquía de roles y scopes
- verificación de email

### Usuarios y roles

- perfil del usuario autenticado
- actualización de perfil
- cambio de contraseña
- gestión de roles y permisos

### Catálogo y escenarios

- catálogo de tecnologías
- detalle de tecnologías
- estimación técnica base
- escenarios predefinidos
- detalle y administración de escenarios

### Simulation service

- crear simulaciones reales
- crear simulaciones desde escenario predefinido
- obtener detalle por id
- listar simulaciones del usuario
- dashboard agregado
- update con recálculo
- soft delete individual y masivo
- reverse geocoding y búsqueda de ubicaciones

### Slice más endurecido hoy

El módulo más trabajado del backend es `simulation_service`.

Actualmente ya quedó reforzado con:

- update real de simulaciones
- OpenAPI consistente
- JavaDoc dirigido en piezas sensibles
- métricas de negocio y observabilidad
- health indicators por dependencia
- soft delete coherente
- mejor separación read/write
- menor leakage de framework en application
- defaults de scenario externalizados a una policy explícita

## Requisitos previos (desarrollo)

Asegúrate de tener instalado:

- **Git**
- **Docker** y **Docker Compose**
- **Java 21**
- (Opcional) **Maven**
  - si no lo tienes, puedes usar el wrapper: `./mvnw` o `./mvnw.cmd`

## Entorno local

Para desarrollo local normalmente solo necesitas definir unas pocas variables de entorno:

- `JWT_SECRET_BASE64`
- `OPENWEATHER_API_KEY` (opcional si no estás validando integración real)

Puedes exportarlas desde tu shell o cargarlas desde un archivo local no versionado si trabajas así en tu entorno.

## Puesta en marcha rápida

### 1. Clonar el repositorio

```bash
git clone https://github.com/LannyRivero/RenewSim-backend.git
cd RenewSim-backend
```

### 2. Levantar la base de datos

```bash
docker compose up -d mysql
```

La base quedará disponible en:

- `localhost:3307`

### 3. Configurar variables mínimas

Linux/macOS:

```bash
export JWT_SECRET_BASE64="<base64-secret>"
export OPENWEATHER_API_KEY="<api-key-opcional>"
```

Windows PowerShell:

```powershell
$env:JWT_SECRET_BASE64="<base64-secret>"
$env:OPENWEATHER_API_KEY="<api-key-opcional>"
```

### 4. Levantar el backend

Linux/macOS:

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3307/renewsim?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3307/renewsim?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

La aplicación quedará disponible en:

- `http://localhost:8080`

### 5. Opción Docker Compose completa

Si prefieres levantar backend + base de datos desde Docker Compose:

```bash
docker compose up --build
```

Servicios:

- Backend API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- MySQL: `localhost:3307`

## Ejecutar tests

Las pruebas de integración usan Testcontainers y el proyecto también tiene tests unitarios, tests web y property-based tests en partes del motor.

```bash
./mvnw test
```

Ejemplos útiles:

```bash
./mvnw verify
./mvnw test "-Dtest=*Simulation*"
```

## Endpoints útiles (dev)

### Actuator

- `http://localhost:8080/actuator`
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/prometheus`

### Swagger / OpenAPI

- UI → `http://localhost:8080/swagger-ui.html`
- Docs → `http://localhost:8080/v3/api-docs`

### Endpoints principales

#### Auth

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/email-verification/verify`

#### Users y roles

- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`
- `PUT /api/v1/users/me/password`
- `GET /api/v1/roles`
- `POST /api/v1/roles/manage`

#### Catálogo

- `GET /api/v1/technologies`
- `GET /api/v1/technologies/{id}`
- `GET /api/v1/scenarios`
- `GET /api/v1/scenarios/{id}`

#### Simulation service

- `POST /api/v1/simulations`
- `POST /api/v1/simulations/from-scenario`
- `GET /api/v1/simulations/{id}`
- `GET /api/v1/simulations/my-simulations`
- `GET /api/v1/simulations/dashboard`
- `PUT /api/v1/simulations/{id}`
- `DELETE /api/v1/simulations/{id}`
- `DELETE /api/v1/simulations/user`
- `GET /api/v1/simulations/locations/reverse`
- `GET /api/v1/simulations/locations/search`

## Cuenta de prueba (seed)

El proyecto incluye una migración con un usuario administrador de desarrollo:

- Email: `admin@renewsim.com`
- Password: `admin123`

Referencia:

- `src/main/resources/db/migration/V9__seed_admin_user.sql`

Estas credenciales existen solo para facilitar el desarrollo local. No deben usarse fuera de desarrollo.

## Observabilidad

El backend expone observabilidad operativa y de negocio.

Hoy podés medir, entre otras cosas:

- latencia y outcome por use case de `simulation_service`
- llamadas a providers externos
- métricas de negocio de creación y recomendación de simulaciones
- degradaciones de snapshots y fallback paths
- health por dependencia (`simulationService`, `openWeatherSimulation`, `pvgisSimulation`)

Para detalle fino:

- `docs/architecture/simulation-observability-queries.md`
- `docs/architecture/simulation-observability-local-stack.md`

## Calidad y CI

El backend está pensado para evolucionar con una base razonable de control técnico.

Actualmente ya cuenta con:

- tests unitarios de domain y application
- web/controller tests
- repository/adapters tests
- integration tests con Testcontainers
- property-based testing con jqwik en partes del motor
- OpenAPI visible para revisar contratos
- observabilidad y métricas para validar comportamiento operativo

## Documentación técnica relevante

- `docs/design/01-architecture-overview.md`
- `docs/requirements/requirements.md`
- `docs/adr/ADR-011-otp-removal-simplified-login.md`
- `docs/adr/ADR-012-simulation-external-resilience-and-fallbacks.md`
- `docs/architecture/simulation-phase7-flows.md`
- `docs/architecture/simulation-observability-queries.md`

## Qué no promete este README

Para evitar desalineaciones, este README no presenta como baseline actual:

- 2FA por OTP en el login
- frontend funcional dentro de este repo
- una URL pública ya desplegada
- un chatbot/IA listo para demo como parte central del backend defendible

Esas piezas pueden existir en roadmap, en otros repos o en visión de producto, pero no forman parte del baseline actual de este backend.

## Estado actual

El backend no está en fase de maqueta. El estado real hoy es:

- backend funcional y defendible
- `simulation_service` endurecido a nivel funcional, documental y arquitectónico
- documentación técnica útil ya existente
- empaquetado final del proyecto todavía mejorable

Lo siguiente con más valor ya no es más refactor fino, sino:

1. alinear `requirements.md` con el sistema real
2. cerrar la narrativa técnica y oral del proyecto
3. preparar mejor packaging, despliegue y material de presentación

## Autor

**Lanny Rivero Canino**

- LinkedIn: `https://www.linkedin.com/in/lannyriverocanino/`
- GitHub: `https://github.com/LannyRivero`

## Licencia

Proyecto bajo licencia MIT.
