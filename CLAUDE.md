# CLAUDE.md — RenewSim Backend

> Archivo de contexto permanente para sesiones con Claude.
> Actualizar tras cada fase completada o decisión arquitectónica relevante.
> Última actualización: Fase 2 en progreso (abril 2026).

---

## IDENTIDAD DEL PROYECTO

**Nombre:** RenewSim — Plataforma de simulación de energías renovables
**Tipo:** Trabajo de Fin de Máster + estándar de producción real
**Objetivo:** Backend Spring Boot con arquitectura hexagonal completa, seguridad real,
testing profesional y criterio técnico defendible ante perfiles Senior/Lead.
**Repositorio:** monorepo, rama principal de integración `dev/v1.2.0`
**Dev único:** Lanny (implementa el código; Claude audita antes de cada commit)

---

## STACK TECNOLÓGICO

| Componente       | Versión / Detalle                                                                  |
|------------------|------------------------------------------------------------------------------------|
| Java             | 21 — Virtual Threads habilitados (`spring.threads.virtual.enabled: true`)          |
| Spring Boot      | 3.2.1                                                                              |
| Base de datos    | MySQL 8.0 (Docker)                                                                 |
| ORM              | JPA / Hibernate + Flyway (migraciones append-only)                                 |
| Mappers          | MapStruct 1.6.2                                                                    |
| Seguridad        | Spring Security 6 + JJWT 0.11.5 (HS512)                                           |
| Cache            | Caffeine 3.1.8                                                                     |
| Resiliencia      | Resilience4j 2.x (circuit breaker + retry + timelimiter)                           |
| Métricas         | Micrometer + Actuator                                                              |
| Documentación    | SpringDoc OpenAPI 2.5.0 (`/swagger-ui.html`)                                       |
| Testing          | JUnit 5 + Mockito + jqwik (PBT) + Testcontainers                                  |
| Cobertura        | JaCoCo (objetivo >= 70% por bounded context)                                       |
| Build            | Maven (`./mvnw`)                                                                   |
| Contenedores     | Docker Desktop + Docker Compose                                                    |
| IDE              | VS Code + Java Language Server                                                     |
| VCS              | Git Bash (MINGW64) en Windows, GitHub                                              |

**Nota JJWT (ADR-003):** Se mantiene 0.11.5. JJWT 0.12.x introduce breaking changes en API.
Auditoría de actualización planificada para Fase 5 con OWASP.

---

## ARQUITECTURA — REGLAS NO NEGOCIABLES

### Estructura de capas (hexagonal)

```
domain/          -> Reglas de negocio puras. Sin Spring. Sin Jakarta. Sin JPA.
application/     -> Casos de uso. Orquesta domain. Depende solo de ports.
infrastructure/  -> Implementaciones técnicas: persistencia, seguridad, email, feign.
web/             -> HTTP <-> Application. DTOs, controllers, validación de entrada.
```

### Ubicaciones canónicas

| Artefacto           | Ubicación                                      |
|---------------------|------------------------------------------------|
| DTOs de entrada      | `web/dto/`                                     |
| DTOs de salida       | `application/result/`                          |
| Mappers             | `application/mapper/` (MapStruct)              |
| Puertos entrada     | `application/port/in/`                         |
| Puertos salida      | `application/port/out/`                        |
| Controllers         | `web/controller/`                              |
| Commands            | `application/command/`                         |
| Adapters JPA        | `infrastructure/persistence/adapter/`          |
| Entities JPA        | `infrastructure/persistence/entity/`           |
| Repos JPA           | `infrastructure/persistence/repo/`             |
| Seguridad           | `infrastructure/security/` / `infrastructure/config/` |

### Invariantes de arquitectura

- El dominio **nunca** importa Spring, Jakarta, Hibernate ni ninguna dependencia técnica.
- Los controllers **nunca** acceden a repositories directamente.
- Los services de application **nunca** conocen `HttpServletRequest`, `ResponseEntity` ni cookies.
- Los DTOs web **nunca** entran al domain. Se mapean en el adapter o en el controller.
- Si hay un acoplamiento entre capas -> es un riesgo P1 que se documenta como ADR.

### Violaciones conocidas activas

| Violación                                                         | ADR     | Estado        |
|-------------------------------------------------------------------|---------|---------------|
| `RoleJpaRepository` inyectado en `UserPersistenceAdapter`         | ADR-006 | Temporal      |
| `FeignRoleConfig` importa `JwtTokenProvider` de auth_service      | D2-06   | P0 pendiente  |
| DTOs de `user_service` usados cross-BC                            | D2-07   | P1 pendiente  |

---

## BOUNDED CONTEXTS

### Estado actual (abril 2026)

| Contexto             | Estado           | Notas                                                           |
|----------------------|------------------|-----------------------------------------------------------------|
| `auth_service`       | Fase 2 activa    | 2FA (step1/step2), JWT, refresh cookie, logout, rate limiting   |
| `user_service`       | Fase 2 activa    | User aggregate, /me, password change, roles                     |
| `role_service`       | Implementado     | CRUD roles, assign/revoke, politicas                            |
| `technology_service` | Implementado     | Value objects, domain service, factory, cache Caffeine 10min    |
| `simulation_service` | Diferido         | Commands y ports existen; sin services ni tests                 |
| `ai_service`         | No iniciado      | Planificado: OpenAI GPT-4o + Anthropic Claude                   |

### Nivel de acoplamiento entre BCs (Task 1.2 analysis)

| BC                   | Consumidores externos | Estado       |
|----------------------|-----------------------|--------------|
| `user_service`       | 9                     | Alto         |
| `role_service`       | 17                    | Muy alto     |
| `auth_service`       | 3                     | Bajo         |
| `technology_service` | 0                     | Aislado      |
| `simulation_service` | Por validar           |              |

### Shared Kernel

`RoleName` vive en `shared/domain/vo/` — extraído de `role_service` (15 usos cross-BC).
Es un value object legitimo en shared kernel.

### Dominio admin seed

- Email: `admin@renewsim.com` | Password: `Admin@2024` | User: `admin`
- Migración: V9 (solo para perfil `local` / `docker`)

---

## FLYWAY — MIGRACIONES

Estado actual (V1-V11):

| Versión | Descripción                           |
|---------|---------------------------------------|
| V1      | users + roles tables                  |
| V2      | auth tables (otp, tokens, blacklist)  |
| V3      | technologies + scenarios              |
| V4      | simulations                           |
| V5      | seed roles (USER, ADMIN)              |
| V6      | ai tables (chat_sessions, messages)   |
| V7      | fix roles seed syntax                 |
| V8      | fix user_roles schema (ManyToMany)    |
| V9      | seed admin user                       |
| V10     | add user profile fields               |
| V11     | fix otp_code column length            |

**Reglas Flyway:**
- Las migraciones son **append-only**. Nunca editar una versión existente.
- Para revertir: crear nueva migración `V{X+1}__revert_{descripción}.sql`.
- Flyway deshabilitado en tests (`spring.flyway.enabled: false` en `application-test.yml`).
- Un reset de BD: bajar contenedor, borrar volumen, reiniciar.
- En CI: `./mvnw flyway:validate` verifica que no haya migraciones modificadas.

---

## SEGURIDAD

### JWT

- Algoritmo: **HS512**
- Claims: `sub` (email), `jti` (UUID), `roles`, `scopes`, `iat`, `nbf`, `exp`
- Access token: 1h (`expiration-seconds: 3600`)
- Refresh token: 7 días (rotación en cada uso, hash SHA-256 en BD)
- Secreto mínimo: 64 bytes (base64 o plaintext)
- Configuración: `security.jwt.*` en `application.yml`
- Blacklist JTI en `token_blacklist` con caché Caffeine

### Refresh token cookie

- Tipo: `ResponseCookie` (Spring) — **NO** `jakarta.servlet.http.Cookie`
- Atributos obligatorios: `HttpOnly=true`, `Secure=true`, `SameSite=Strict`,
  `Path=/api/v1/auth/refresh`, `MaxAge=7d`
- Deuda activa D2-02: actualmente usa `jakarta.servlet.Cookie` sin SameSite

### Rate limiting

- Login step1: 5 intentos fallidos en 60s -> bloqueo 300s
- OTP fallidos: 3 intentos -> bloqueo 15min + invalidar OTP activo
- Resend OTP: max 3 reenvíos en 15min
- Respuesta: HTTP 429 con `retryAfterSeconds`

### Scopes (YamlScopePolicy)

```
USER         -> read/write/compare/export/delete:simulations
ADMIN        -> todo lo anterior + read/manage:users
SERVICE_AUTH -> user:write, user:read
```

### Role hierarchy

```
ROLE_ADMIN > ROLE_USER
```

### Endpoints públicos

```
POST /api/v1/auth/login
POST /api/v1/auth/register
POST /api/v1/auth/login/step1
POST /api/v1/auth/login/step2
POST /api/v1/auth/activate
POST /api/v1/auth/resend-otp
POST /api/v1/auth/refresh
GET  /api/v1/users/exists
GET  /api/v1/users/by-username
GET  /actuator/health
GET  /swagger-ui/**
GET  /api-docs/**
```

---

## PERFILES SPRING

| Perfil   | Uso                                          |
|----------|----------------------------------------------|
| `local`  | Desarrollo local (VS Code, Git Bash)          |
| `docker` | Docker Compose                               |
| `prod`   | Producción                                   |
| `test`   | Tests automáticos (Flyway off, Testcontainers)|

**Importante:** El perfil `dev` **no existe**. Usar `local`.
`spring.profiles.active` está **prohibido** dentro de archivos de perfil específicos (Spring Boot 3.x).

---

## MODELO DE DOMINIO — RESUMEN

### Aggregates principales

**User** (`user_service/domain/model/`):
- Campos: id, email, passwordHash, fullName, phone, status (ACTIVE/INACTIVE/SUSPENDED),
  roles (Set<RoleName>), createdAt, activatedAt
- Métodos: `create()`, `reconstitute()`, `activate()`, `suspend()`,
  `addRole()`, `removeRole()`, `updateProfile()`, `changePassword()`
- Invariantes: email válido (regex), passwordHash formato BCrypt ($2a/$2b/$2y$), status no nulo

**Simulation** (`simulation_service/domain/model/`) — diferido:
- Estados: `DRAFT -> COMPLETED -> ARCHIVED` (transiciones invertidas prohibidas: HTTP 409)
- Invariantes: capacityKw en [1, 10000], initialInvestment > 0,
  lat en [-90, 90], lon en [-180, 180]
- Formula energía: capacidad × eficiencia × 8760 × factorClimático
- Cálculos: ROI, Payback, CO2, NPV (VAN), IRR (TIR)

### Value Objects clave (en `shared/domain/vo/`)

```
Money(BigDecimal amount, String currency)       -> amount >= 0
Location(double latitude, double longitude)     -> lat en [-90,90], lon en [-180,180]
ClimateData(irradiation, windSpeed, temperature)
Email(String value)
Password(String value)
RoleName (enum: USER, ADMIN, ANALYST)
```

### Domain entities auth

```
OtpCode          -> id, userId, codeHash, expiresAt (5min), used, Purpose(LOGIN)
                    métodos: isValid(), isExpired(), markUsed()
RefreshToken     -> id, userId, tokenHash, expiresAt (7d), revoked
                    métodos: isValid()
ActivationToken  -> id, userId, tokenHash, expiresAt (24h), used
                    métodos: isValid(), markUsed()
```

---

## DISEÑO DE BASE DE DATOS

### Tablas principales

```
users               -> id, email(UK), password_hash, full_name, phone, status, created_at, activated_at
roles               -> id, name(UK), description, created_at
user_roles          -> user_id(FK), role_id(FK), assigned_at  [PK compuesta]
refresh_tokens      -> id, user_id(FK), token_hash(UK), expires_at, revoked
token_blacklist     -> id, jti(UK), expires_at
otp_codes           -> id, user_id(FK), code_hash, expires_at, used
activation_tokens   -> id, user_id(FK,UK), token_hash(UK), expires_at, used
technologies        -> id, name, energy_type(ENUM), efficiency, base_cost_per_kw, lifespan_years, is_active
scenarios           -> id, name, technology_id(FK), climate_profile(JSON), is_active
simulations         -> id, user_id(FK), technology_id(FK), name, status(ENUM), lat, lon, capacity_kw, ...results
chat_sessions       -> id, user_id(FK), session_id(UK), last_active
chat_messages       -> id, session_id(FK), role(ENUM), content, tokens_used
```

### Índices compuestos críticos

- `idx_otp_user_expires (user_id, expires_at)`
  -> login step2: `WHERE user_id=? AND expires_at>NOW() AND used=FALSE`
- `idx_simulations_user_status (user_id, status)`
  -> my-simulations: `WHERE user_id=? AND status!='ARCHIVED'`
- `idx_blacklist_jti (jti)`
  -> JWT filter: `SELECT EXISTS(... WHERE jti=?)`

---

## COMANDOS FRECUENTES

### Build y tests

```bash
./mvnw clean install
./mvnw clean test
./mvnw jacoco:check
./mvnw jacoco:report
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Docker

```bash
docker compose up -d
docker compose down -v
docker compose logs -f backend
```

### Git (Conventional Commits)

```bash
git checkout -b feat/nombre-tarea
git add -p
git commit -m "feat(auth): descripción"
git push origin feat/nombre-tarea
```

**Tipos:** `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `perf`
**Scopes:** `auth`, `user`, `role`, `tech`, `sim`, `shared`, `infra`, `test`, `ci`

### Windows-específico (Git Bash / MINGW64)

```bash
# Matar proceso en puerto 8080
powershell "Stop-Process -Id $(netstat -ano | findstr :8080 | awk '{print $5}') -Force"

# CRLF noise en git status -> descartar
git checkout -- .

# Después de git mv -> recargar VS Code
# Command Palette -> "Clean Java Language Server Workspace"
```

---

## CONVENCIONES DE CÓDIGO Y NAMING

### Java

- **Records** para Commands, Results y DTOs inmutables.
- **Factory methods** en entidades: `create(...)` nuevas instancias, `reconstitute(...)` desde persistencia.
- **Métodos con semántica de negocio:** `activate()`, `suspend()`, `markUsed()`, `isValid()`.
- **Validación de invariantes en constructor** — nunca diferida.
- **No Lombok en domain layer** — solo en infrastructure/application.

### Naming

| Artefacto          | Convención               | Ejemplo                     |
|--------------------|---------------------------|-----------------------------|
| Use case (port in) | `VerbNounUseCase`         | `LoginStep1UseCase`         |
| Service (impl)     | `VerbNounService`         | `LoginStep1Service`         |
| Command            | `VerbNounCommand`         | `LoginStep1Command`         |
| Result DTO         | `VerbNounResultDTO`       | `LoginStep1ResultDTO`       |
| Port out           | `NounPort` / `NounGateway`| `OtpCodeRepositoryPort`     |
| Adapter            | `NounPersistenceAdapter`  | `UserPersistenceAdapter`    |
| JPA Repo           | `NounJpaRepository`       | `UserJpaRepository`         |
| JPA Entity         | `NounEntity`              | `UserEntity`                |
| Controller         | `NounController`          | `AuthController`            |
| Request DTO        | `VerbNounRequestDTO`      | `LoginStep1RequestDTO`      |

### Tests

- Método: `methodName_condition_expectedBehavior()`
- `@DisplayName` descriptivo.
- Un `@Test` = un comportamiento verificado.
- `TestSecurityConfig` importado en **todo** `@WebMvcTest`.

---

## ADRs ACTIVOS Y DEUDA CONOCIDA

### ADR-001 — Arquitectura Hexagonal

**Decisión:** Puertos y Adaptadores.
**Trade-off:** Más interfaces y clases; dominio testeable sin Spring/BD.

### ADR-002 — MySQL vs PostgreSQL

**Decisión:** MySQL 8.0.
**Trade-off:** PostgreSQL tiene mejor soporte para tipos avanzados (PostGIS futuro).

### ADR-003 — JJWT 0.11.5 vs 0.12.x

**Decisión:** Mantener 0.11.5 hasta Fase 5.
**Acción Fase 5:** Auditoría OWASP + migración.

### ADR-004 — Deuda de tests (72 fallos)

| Categoría                               | Cantidad | Solución                                          |
|-----------------------------------------|----------|---------------------------------------------------|
| NPE por `@Mock` faltantes               | 13       | Añadir mocks en setup                             |
| `@WebMvcTest` sin `TestSecurityConfig`  | 36       | `@Import(TestSecurityConfig.class)` en cada clase |
| Assertions / lógica incorrecta          | 15       | Revisar expectativas vs comportamiento real       |
| Tests Docker-dependientes               | 8        | Configurar Testcontainers correctamente           |

### ADR-005 — Persistencia de enums (RoleName)

**Decisión:** `@Enumerated(EnumType.STRING)` vía `@ManyToMany` con `RoleEntity`.
**Reemplaza:** `@ElementCollection Set<String>` (eliminado en V8).

### ADR-006 — `RoleJpaRepository` en `UserPersistenceAdapter`

**Estado:** Violación temporal. Plan: reemplazar con `RoleCatalogPort` antes de Fase 3.

### Deuda Fase 2 activa

| ID    | Descripción                                                             | Prioridad |
|-------|-------------------------------------------------------------------------|-----------|
| D2-01 | `EmailPort` no implementado — OTPs generados pero no enviados           | P0        |
| D2-02 | Cookie refresh token usa `jakarta.servlet.Cookie` sin SameSite          | P0        |
| D2-03 | Tests integración Testcontainers (flujo 2FA completo) ausentes          | P1        |
| D2-04 | Tests MockMvc seguridad (401/403/429) ausentes                          | P1        |
| D2-05 | `BCryptPasswordEncoder(14)` sin ADR — latencia ~1-2s en login           | P2        |
| D2-06 | `FeignRoleConfig` importa `JwtTokenProvider` — leak infra cross-BC      | P0        |
| D2-07 | DTOs de `user_service` usados cross-BC                                  | P1        |

---

## REGLAS DE TRABAJO CON CLAUDE

### Protocolo de sesión

1. **Compartir código actual primero** — siempre antes de pedir generación.
2. **Mientras se comparte código y no se dice "LISTO":** Claude solo recibe contexto.
3. **"LISTO"** = señal de que el código está listo para auditoría completa.
4. **Una tarea por sesión** — sesión nueva por tarea.

### Formato de auditoría (tras "LISTO")

1. Veredicto: Listo / Casi / No listo
2. Supuestos realizados
3. Top riesgos (P0 / P1 / P2)
4. Qué está bien y debe mantenerse
5. Qué falta o está mal (priorizado y accionable)
6. Cambios concretos recomendados
7. Tests faltantes (unit / integration / E2E / security)
8. Checklist DoD
9. Próximo paso + commit sugerido (Conventional Commits)
10. Comprensión: propósito, trade-off principal, consecuencia clave

### Estándar de producción (no negociable)

- Separación estricta de capas
- Dominio libre de frameworks
- Seguridad realista (JWT, roles, scopes)
- Tests >= 70% con sentido real
- OpenAPI documentado
- CI verde
- README y ADRs claros

### Entregas de código

- Claude entrega **archivos completos**, no diffs parciales.
- Claude **no genera código** hasta tener el archivo actual del proyecto.

---

## PROXIMAS TAREAS (orden de prioridad)

### Fase 2 — Pendientes para cerrar

1. [P0] Implementar `EmailPort` + `LoggingEmailAdapter` (@Profile("local"))
2. [P0] Fix cookie refresh token -> `ResponseCookie` con `SameSite=Strict`
3. [P0] Fix `FeignRoleConfig` — reemplazar import de `JwtTokenProvider`
4. [P1] Tests integración Testcontainers — flujo completo register -> activate -> step1 -> step2
5. [P1] Tests MockMvc seguridad — 401 / 403 / 429
6. [P1] Resolver ADR-004: 36 tests @WebMvcTest + 13 NPE + 15 assertion failures
7. [P2] ADR para `BCryptPasswordEncoder(14)`
8. [P2] Resolver ADR-006: reemplazar `RoleJpaRepository` con `RoleCatalogPort`

### Fases futuras

- Fase 3: Motor de simulación (simulation_service)
- Fase 4: Frontend React + TypeScript
- Fase 5: AI Service — OpenAI + Anthropic
- Fase 6: CI/CD GitHub Actions, Docker production
- Fase 7: Performance, E2E Playwright
- Fase 8: Documentación final, defensa