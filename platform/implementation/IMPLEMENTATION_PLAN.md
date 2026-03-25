# Implementation Tasks — RenewSim Platform

## Overview

Plan de implementación completo para la plataforma RenewSim.
Plazo: 15 de abril de 2026 (21 días desde el 25 de marzo de 2026).
Stack: Java 21 + Spring Boot 3.2.1 (backend hexagonal) · React 18 + TypeScript + Vite (frontend).

---

## Fase 1: Backend Foundation

- [ ] 1. Fase 1: Backend Foundation
  - [ ] 1.1 Crear proyecto Spring Boot 3.2.1 con Java 21 y dependencias completas
    - Verificar/actualizar `pom.xml` con: spring-boot-starter-web, data-jpa, mysql-connector-j, security, validation, actuator, flyway-core, jjwt-api/impl/jackson, caffeine, resilience4j-spring-boot3, mapstruct, mapstruct-processor, jqwik, testcontainers-mysql, springdoc-openapi-starter-webmvc-ui, itext7-core
    - Configurar MapStruct annotation processor en `pom.xml`: añadir `maven-compiler-plugin` con `annotationProcessorPaths` incluyendo `mapstruct-processor` y `lombok-mapstruct-binding` (si se usa Lombok); asegurar que `mapstruct` y `mapstruct-processor` tienen el mismo version property `${mapstruct.version}`
    - Habilitar Virtual Threads en `application.yml`: `spring.threads.virtual.enabled=true`
    - _Requirements: 17.1, 17.2_

  - [ ] 1.2 Configurar estructura hexagonal de paquetes por bounded context
    - Crear directorios `domain/{model,service,repository,exception,policy}`, `application/{port/in,port/out,command,service,mapper}`, `infrastructure/{persistence,config,security,client}`, `web/{controller,dto}` para cada bounded context: `auth_service`, `user_service`, `role_service`, `technology_service`, `simulation_service`, `ai_service`, `shared`
    - _Requirements: 21.2 (dominio sin Spring)_

  - [ ] 1.3 Configurar `application.yml` con perfiles local, docker y prod
    - Perfil `local`: datasource localhost:3306, logging DEBUG, Flyway enabled
    - Perfil `docker`: datasource `db:3306`, logging INFO
    - Perfil `prod`: datasource via env vars, logging WARN, Actuator endpoints restringidos
    - _Requirements: 19.1_

  - [ ] 1.4 Crear migraciones Flyway V1–V6 (todas las tablas del sistema)
    - `V1__create_users_and_roles.sql`: tablas `users`, `roles`, `user_roles` con índices `idx_users_email`, `idx_users_status`
    - `V2__create_auth_tables.sql`: tablas `refresh_tokens`, `token_blacklist`, `otp_codes`, `activation_tokens` con índices compuestos según diseño de BD
    - `V3__create_technologies_and_scenarios.sql`: tablas `technologies` (EnergyType ENUM, is_active, índices) y `scenarios` (climate_profile JSON, FK a technologies)
    - `V4__create_simulations.sql`: tablas `simulations` (todos los campos del diseño, índices compuestos) y `simulation_share_tokens` (token UNIQUE, expires_at)
    - `V5__seed_roles.sql`: INSERT roles USER, ADMIN, ANALYST
    - `V6__create_ai_tables.sql`: tablas `chat_sessions` (session_id UNIQUE, índices) y `chat_messages` (role ENUM, content TEXT, índice por session_id)
    - _Requirements: 1.1, 2.1, 2.4, 5.1, 6.1, 8.1, 15.1_

  - [ ] 1.5 Crear value objects base del dominio compartido
    - `Email` record con validación de formato RFC 5322
    - `Password` record con validación (≥8 chars, mayúscula, dígito, símbolo)
    - `Location` record con validación lat[-90,90] lon[-180,180] — lanzar `InvalidLocationException`
    - `Money` record con `BigDecimal amount` + `String currency`, validar amount ≥ 0
    - `ClimateData` record con `avgSolarIrradiation`, `avgWindSpeed`, `avgTemperature`
    - Sin anotaciones Spring en ninguno de estos records
    - _Requirements: 6.8, 6.9, 6.10_

  - [ ] 1.6 Configurar JaCoCo con threshold ≥70% en `pom.xml`
    - Plugin `jacoco-maven-plugin` con execution `prepare-agent` + `report` + `check`
    - Regla `PACKAGE` / `LINE` / `COVEREDRATIO` ≥ 0.70
    - Excluir paquetes `*.dto`, `*.config`, `*.mapper` del check
    - _Requirements: (estrategia de testing)_

  - [ ] 1.7 Configurar GitHub Actions CI (job `backend-tests` con MySQL service container)
    - Archivo `.github/workflows/ci.yml` con job `backend-tests`
    - Service container `mysql:8.0` con health check
    - Steps: checkout → setup-java 21 temurin → `./mvnw clean test` → `jacoco:report` → `jacoco:check`
    - Variables de entorno: `SPRING_DATASOURCE_URL`, `JWT_SECRET`
    - _Requirements: 19.1_

  - [ ] 1.8 Configurar `GlobalExceptionHandler` con envelope de error consistente
    - `@RestControllerAdvice` en `shared/web/controller/GlobalExceptionHandler`
    - Manejar: `MethodArgumentNotValidException` → 400 con `fieldErrors`, `EntityNotFoundException` → 404, `AccessDeniedException` → 403, `ConflictException` → 409, `RateLimitExceededException` → 429, `AIServiceUnavailableException` → 503
    - Envelope: `{ timestamp, status, errorCode, message, path, fieldErrors? }`
    - _Requirements: 1.3, 1.4, 2.2, 4.5, 6.8_

  - [ ] 1.9 Configurar OpenAPI/Swagger con SpringDoc
    - Bean `OpenAPI` con info (título, versión, descripción), `SecurityScheme` BearerAuth JWT
    - Anotaciones `@Tag` en controllers, `@Operation` en endpoints principales
    - URL: `/swagger-ui.html` disponible en perfiles local y docker
    - _Requirements: (documentación API)_

  - [ ] 1.10 Crear Dockerfile backend multi-stage
    - Stage `build`: `eclipse-temurin:21-jdk-alpine`, `COPY pom.xml` + `mvnw` + `.mvn`, `RUN ./mvnw dependency:go-offline`, `COPY src`, `RUN ./mvnw clean package -DskipTests`
    - Stage `runtime`: `eclipse-temurin:21-jre-alpine`, usuario no-root `renewsim`, `COPY --from=build app.jar`
    - `HEALTHCHECK`: `wget --spider http://localhost:8080/actuator/health`
    - `ENTRYPOINT`: JVM flags `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`
    - Verificar que `docker build -t renewsim-backend .` completa sin errores
    - _Requirements: (deployment)_

---

## Fase 2: User & Auth Service

- [ ] 2. Fase 2: User & Auth Service
  - [ ] 2.1 Implementar `User` aggregate root con invariantes (domain layer, sin Spring)
    - Clase `User` en `user_service/domain/model/` con campos: id, email, passwordHash, fullName, phone, status (`UserStatus` enum: ACTIVE/INACTIVE/SUSPENDED), roles, createdAt, activatedAt
    - Métodos: `activate()`, `suspend()`, `hasRole(String)`, `addRole(Role)`, `removeRole(Role)`
    - Validar que email no sea nulo, passwordHash no sea nulo
    - _Requirements: 1.1, 1.7_

  - [ ] 2.2 Implementar `UserRepository` port (interfaz en `domain/repository`)
    - Métodos: `save(User)`, `findById(Long)`, `findByEmail(String)`, `existsByEmail(String)`, `delete(Long)`
    - Sin anotaciones Spring ni JPA
    - _Requirements: 1.1, 3.1_

  - [ ] 2.3 Implementar `UserJpaRepository` adapter (infrastructure/persistence)
    - `UserEntity` con anotaciones JPA mapeando tabla `users`
    - `UserJpaRepository extends JpaRepository<UserEntity, Long>`
    - `UserRepositoryAdapter implements UserRepository` usando MapStruct `UserMapper` para conversión Entity ↔ Domain
    - _Requirements: 1.1_

  - [ ] 2.4 Implementar `RegisterUserCommand` + `RegisterUserUseCase`
    - `RegisterUserCommand` record: email, rawPassword, fullName
    - `RegisterUserUseCase` interfaz en `application/port/in/`
    - `RegisterUserService` implementación: validar email único (409 si existe), hashear password con BCrypt strength 12, crear User INACTIVE con rol USER, persistir, disparar evento de activación
    - _Requirements: 1.1, 1.3, 1.7_

  - [ ] 2.5 Implementar `POST /api/v1/users/register` controller + DTOs
    - `RegisterRequestDTO` record: `@Email @NotBlank email`, `@NotBlank @Size(min=8,max=100) password`, `@NotBlank fullName`
    - `RegisterResponseDTO` record: id, email, fullName, status, message
    - `UserController` con `@PostMapping("/register")` → HTTP 201
    - _Requirements: 1.1, 1.3, 1.4_

  - [ ] 2.6 Implementar `ActivationToken` domain entity + `POST /api/v1/users/activate`
    - `ActivationToken` entity: id, userId, tokenHash, expiresAt (24h), used
    - Método `isExpired()`, `isValid()`
    - `ActivateAccountUseCase`: buscar token por hash, validar no expirado y no usado, marcar usado, cambiar User a ACTIVE
    - Controller `POST /api/v1/users/activate` con body `{ token }` → HTTP 200
    - _Requirements: 1.2, 1.5, 1.6_

  - [ ] 2.7 Implementar `OtpGenerator` domain service (SecureRandom, 6 dígitos)
    - `OtpGenerator` en `auth_service/domain/service/` sin Spring
    - Método `generate()` → String de 6 dígitos usando `SecureRandom`
    - Método `hash(String otp)` → BCrypt hash del código
    - _Requirements: 2.1_

  - [ ] 2.8 Implementar `OtpCode` domain entity con `isExpired()` e `isValid()`
    - `OtpCode` entity: id, userId, codeHash, expiresAt (5 min), used
    - `isExpired()`: `LocalDateTime.now().isAfter(expiresAt)`
    - `isValid()`: `!used && !isExpired()`
    - _Requirements: 2.1, 2.5_

  - [ ] 2.9 Implementar `EmailPort` (interfaz) + `SendGridEmailAdapter` (infrastructure)
    - `EmailPort` en `shared/domain/port/out/`: `sendOtpEmail(String to, String otpCode)`, `sendActivationEmail(String to, String token)`
    - `SendGridEmailAdapter` usando `JavaMailSender` (Spring Mail) con configuración SMTP SendGrid
    - `@PostConstruct` en `SendGridEmailAdapter`: validar que `MAIL_HOST`, `MAIL_USERNAME` y `MAIL_PASSWORD` no están vacíos; lanzar `IllegalStateException("Email configuration is missing")` si alguno falta — impide arranque con configuración incompleta
    - Templates de email en texto plano (sin HTML complejo)
    - _Requirements: 1.2, 2.1_

  - [ ] 2.10 Implementar `POST /api/v1/auth/login/step1` (valida credenciales, genera OTP, envía email)
    - `LoginStep1RequestDTO` record: `@Email email`, `@NotBlank password`
    - `AuthServiceImpl.step1()`: cargar User por email, verificar BCrypt, verificar ACTIVE, generar OTP, persistir hash en `otp_codes`, enviar email, retornar `{ message, expiresInSeconds: 300 }`
    - Respuesta genérica 200 sin revelar si email existe
    - _Requirements: 2.1, 2.2_

  - [ ] 2.11 Implementar `JwtTokenProvider` con JTI, HS512, access 1h + refresh 7d
    - `JwtTokenProvider` en `auth_service/infrastructure/security/`
    - `generateAccessToken(Long userId, String email, List<String> roles)` → JWT con claims: sub, jti (UUID), roles, iat, exp (1h)
    - `generateRefreshToken(Long userId)` → JWT firmado HS512 exp 7d
    - `validateToken(String token)` → boolean
    - `extractJti(String token)`, `extractUsername(String token)`, `extractRoles(String token)`
    - `@PostConstruct` validar secreto ≥ 32 chars
    - _Requirements: 2.4, 2.13, 18.2_

  - [ ] 2.12 Implementar `RefreshToken` domain entity
    - `RefreshToken` entity: id, userId, tokenHash, expiresAt (7d), revoked
    - `isValid()`: `!revoked && LocalDateTime.now().isBefore(expiresAt)`
    - `RefreshTokenRepository` port + JPA adapter
    - _Requirements: 2.9, 2.10_

  - [ ] 2.13 Implementar `POST /api/v1/auth/login/step2` (valida OTP, genera JWT, cookie HttpOnly)
    - `LoginStep2RequestDTO` record: `@Email email`, `@NotBlank otpCode`
    - `AuthServiceImpl.step2()`: validar OTP (hash match, no expirado, no usado), marcar OTP como usado, generar accessToken + refreshToken, persistir refreshToken hash, retornar `AuthResponseDTO`
    - Refresh token como `ResponseCookie` HttpOnly, Secure, SameSite=Strict, path=/api/v1/auth/refresh, maxAge=7d
    - _Requirements: 2.4, 2.5_

  - [ ] 2.14 Implementar `POST /api/v1/auth/resend-otp` con rate limiting
    - Validar que el usuario existe y está ACTIVE
    - Invalidar OTP anterior (marcar used=true)
    - Verificar contador de reenvíos (máx 3 en 15 min) con Caffeine cache
    - Generar y enviar nuevo OTP
    - _Requirements: 2.7, 2.8_

  - [ ] 2.15 Implementar `POST /api/v1/auth/refresh` (rota refresh token)
    - Leer refresh token de cookie HttpOnly
    - Validar hash en BD, verificar `isValid()`
    - Revocar token anterior, generar nuevo par access+refresh
    - _Requirements: 2.9, 2.10_

  - [ ] 2.16 Implementar `POST /api/v1/auth/logout` (blacklist JTI + revoca refresh)
    - Extraer JTI del access token del header Authorization
    - Insertar JTI en `token_blacklist` con `expires_at` = expiración natural del token
    - Revocar refresh token del usuario (marcar `revoked=true`)
    - _Requirements: 2.11_

  - [ ] 2.17 Implementar `JwtAuthenticationFilter` (OncePerRequestFilter + blacklist check)
    - Extraer Bearer token del header Authorization
    - Validar firma y expiración con `JwtTokenProvider`
    - Verificar JTI no está en blacklist (consulta BD con caché Caffeine)
    - Poblar `SecurityContextHolder` con `UsernamePasswordAuthenticationToken`
    - _Requirements: 18.1, 18.2_

  - [ ] 2.18 Implementar `SecurityConfig` (Spring Security 6, CORS, headers de seguridad)
    - `SessionCreationPolicy.STATELESS`, CSRF disabled
    - `authorizeHttpRequests`: endpoints públicos (login/step1, step2, resend-otp, refresh, register, activate, /shared/**, /actuator/health), ADMIN-only (POST/PUT/DELETE /technologies, POST /roles, /users/{id}/roles/**), resto authenticated
    - CORS: orígenes permitidos desde properties, allowCredentials=true
    - Headers: HSTS, X-Content-Type-Options, X-Frame-Options DENY
    - _Requirements: 18.1, 18.3, 18.5_

  - [ ] 2.19 Implementar `LoginRateLimiter` con Caffeine (5 intentos → bloqueo 15 min)
    - `Cache<String, Integer>` con `expireAfterWrite(15, MINUTES)`
    - `recordFailedAttempt(String email)`: incrementar contador, lanzar `RateLimitExceededException` si ≥ 5
    - `resetAttempts(String email)`: invalidar entrada en cache tras login exitoso
    - _Requirements: 2.3_

  - [ ] 2.20 Implementar `GET /api/v1/users/me` + `PUT /api/v1/users/me` + `PUT /api/v1/users/me/password`
    - `GET /me`: retornar `UserProfileDTO` (id, email, fullName, phone, roles, status, createdAt)
    - `PUT /me`: actualizar fullName y phone, retornar perfil actualizado; HTTP 409 si email ya existe
    - `PUT /me/password`: verificar currentPassword con BCrypt, hashear newPassword, revocar todos los refresh tokens del usuario
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [ ] 2.21 Implementar `Role` aggregate + `RoleRepository` + `GET /api/v1/roles` + `POST /api/v1/roles`
    - `Role` entity: id, name, description, createdAt
    - `RoleRepository` port + JPA adapter
    - `GET /api/v1/roles` → lista de roles (solo ADMIN)
    - `POST /api/v1/roles` → crear rol, HTTP 201; HTTP 409 si nombre duplicado
    - _Requirements: 4.1, 4.2, 4.3_

  - [ ] 2.22 Implementar `POST/DELETE /api/v1/users/{userId}/roles/{roleId}`
    - `POST`: asignar rol a usuario; HTTP 404 si usuario o rol no existe
    - `DELETE`: quitar rol a usuario; HTTP 404 si asignación no existe
    - Solo accesible por ADMIN
    - _Requirements: 4.4, 4.5, 4.6_

  - [ ]* 2.23 Tests unitarios: User aggregate, OtpGenerator, JwtTokenProvider, LoginRateLimiter
    - `UserTest`: invariantes, transiciones de estado, addRole/removeRole
    - `OtpGeneratorTest`: output siempre 6 dígitos, distribución uniforme (100 muestras)
    - `JwtTokenProviderTest`: generar → validar → extraer claims round-trip; token expirado → false; secreto corto → excepción en @PostConstruct
    - `LoginRateLimiterTest`: 4 intentos → no bloqueo; 5 intentos → RateLimitExceededException; reset tras éxito
    - Cobertura objetivo ≥80% en `auth_service` y `user_service`
    - _Requirements: 2.1, 2.3, 2.4_

  - [ ]* 2.24 Tests de integración con Testcontainers: flujo completo registro → activación → login 2FA
    - `@SpringBootTest` + `@Testcontainers` con `MySQLContainer`
    - Flujo: POST /register → obtener token de BD → POST /activate → POST /login/step1 → obtener OTP de BD → POST /login/step2 → verificar accessToken válido
    - _Requirements: 1.1, 1.5, 2.1, 2.4_

  - [ ]* 2.25 Tests de seguridad MockMvc: 401/403/429 en endpoints protegidos
    - Sin JWT → 401 en GET /users/me
    - JWT de USER en POST /roles → 403
    - 5 intentos fallidos en /login/step1 → 429 con `retryAfterSeconds`
    - 3 intentos OTP fallidos → 429
    - _Requirements: 18.1, 18.3, 2.3, 2.6_

  - [ ]* 2.26 Tests E2E con RestAssured: flujo completo de extremo a extremo
    - `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@Testcontainers` con `MySQLContainer`
    - Flujo completo: POST /register → POST /activate → POST /login/step1 → POST /login/step2 → POST /simulations → GET /simulations/{id} → GET /simulations/{id}/report → POST /auth/logout
    - Verificar que el access token retornado es un JWT válido firmado con HS512
    - Verificar que GET /simulations/{id} retorna HTTP 401 tras logout
    - Verificar que `energyGeneratedAnnual > 0` y `roiPercentage` es un número en la respuesta de creación
    - _Requirements: 6.1, 6.2, 9.1, 2.11_

- [ ] 2.27 Checkpoint — Fase 2 completa
  - Asegurar que todos los tests pasan (`./mvnw clean test`)
  - Verificar cobertura JaCoCo ≥70% (`./mvnw jacoco:check`)
  - Probar flujo 2FA manualmente con Swagger UI
  - Preguntar al usuario si hay dudas antes de continuar

---

## Fase 3: Technology & Scenario Service

- [ ] 3. Fase 3: Technology & Scenario Service
  - [ ] 3.1 Crear seeds Flyway V7–V8 (las tablas ya existen desde Fase 1)
    - `V7__seed_technologies.sql`: INSERT 3 tecnologías (Panel Solar Fotovoltaico efficiency=0.1850 baseCostPerKw=1200 lifespan=25, Turbina Eólica Terrestre efficiency=0.3500 baseCostPerKw=1500 lifespan=20, Microcentral Hidroeléctrica efficiency=0.8500 baseCostPerKw=2500 lifespan=40)
    - `V8__seed_scenarios.sql`: INSERT 3 escenarios predefinidos referenciando tecnologías de V7 con climate_profile JSON (ver Anexo B de requirements.md)
    - Verificar que `./mvnw flyway:info` muestra V7 y V8 como Pending antes de aplicar
    - _Requirements: 5.1, 8.1_

  - [ ] 3.2 Implementar `Technology` aggregate (domain layer) con `EnergyType` enum
    - `EnergyType` enum: SOLAR, WIND, HYDRO, BIOMASS, GEOTHERMAL
    - `Technology` entity: id, name, energyType, efficiency (0–1), baseCostPerKw, lifespanYears, maintenanceCostPct, description, isActive, createdAt, updatedAt
    - Método `deactivate()` para soft delete
    - Sin anotaciones Spring
    - _Requirements: 5.1, 5.4_

  - [ ] 3.3 Implementar `TechnologyRepository` port + JPA adapter
    - Port: `save(Technology)`, `findById(Long)`, `findAll(Pageable)`, `findByEnergyType(EnergyType, Pageable)`, `findAllActive(Pageable)`
    - `TechnologyEntity` con anotaciones JPA, `TechnologyMapper` MapStruct
    - `TechnologyRepositoryAdapter` implementando el port
    - _Requirements: 5.1, 5.2_

  - [ ] 3.4 Implementar Caffeine cache `@Cacheable` con TTL 10 min + invalidación en write
    - `CacheConfig` bean con `CaffeineCacheManager`, cache `technologies` con `expireAfterWrite(10, MINUTES)`, maximumSize 500
    - `@Cacheable("technologies")` en `getTechnologies()` y `getTechnologyById()`
    - `@CacheEvict(value="technologies", allEntries=true)` en `createTechnology()` y `updateTechnology()`
    - _Requirements: 5.6_

  - [ ] 3.5 Implementar `GET /api/v1/technologies` (paginado, filtro por energyType) + `GET /api/v1/technologies/{id}`
    - `TechnologyResponseDTO` con todos los campos de Technology
    - `GET /technologies?page=0&size=20&energyType=SOLAR` → `Page<TechnologyResponseDTO>`
    - `GET /technologies/{id}` → `TechnologyResponseDTO`; HTTP 404 si no existe
    - _Requirements: 5.1, 5.2, 5.3_

  - [ ] 3.6 Implementar `POST`, `PUT` y `DELETE` (soft delete) en `/api/v1/technologies` — solo ADMIN
    - `TechnologyRequestDTO` con validaciones `@NotBlank`, `@DecimalMin`, `@DecimalMax`
    - `POST /technologies` → HTTP 201 + invalidar caché
    - `PUT /technologies/{id}` → HTTP 200 + invalidar caché
    - `DELETE /technologies/{id}` → llamar `deactivate()` + HTTP 204 (soft delete, `is_active=false`)
    - _Requirements: 5.4, 5.5, 5.6_

  - [ ] 3.7 Implementar `Scenario` aggregate (domain layer)
    - `Scenario` entity: id, name, description, technologyId, defaultCapacityKw, defaultInvestment (Money), defaultTariff, defaultConsumption, climateProfile (ClimateData), isActive
    - Sin anotaciones Spring
    - _Requirements: 8.1, 8.2_

  - [ ] 3.8 Implementar `ScenarioRepository` port + JPA adapter
    - Port: `save(Scenario)`, `findById(Long)`, `findAllActive()`
    - `ScenarioEntity` con `climate_profile` como `@Column(columnDefinition="JSON")` mapeado a `ClimateData` via `@Convert`
    - _Requirements: 8.1, 8.2_

  - [ ] 3.9 Implementar endpoints de Scenario
    - `GET /api/v1/scenarios` → lista de escenarios activos
    - `GET /api/v1/scenarios/{id}` → detalle; HTTP 404 si no existe
    - `POST /api/v1/scenarios` (ADMIN) → HTTP 201
    - `PUT /api/v1/scenarios/{id}` (ADMIN) → HTTP 200
    - _Requirements: 8.1, 8.2, 8.3, 8.5_

  - [ ]* 3.10 Tests unitarios + integración con Testcontainers para Technology y Scenario
    - `TechnologyTest`: deactivate(), validaciones de eficiencia
    - `TechnologyRepositoryIntegrationTest` con Testcontainers: save → findById → findByEnergyType
    - `TechnologyCacheTest`: verificar que segunda llamada usa caché (mock del repositorio)
    - Cobertura objetivo ≥70% en `technology_service`
    - _Requirements: 5.1, 5.6_

- [ ] 3.11 Checkpoint — Fase 3 completa
  - Asegurar que todos los tests pasan
  - Verificar seeds en BD local con `docker compose up`
  - Preguntar al usuario si hay dudas antes de continuar

---

## Fase 4: Simulation Engine & Core Logic

- [ ] 4. Fase 4: Simulation Engine & Core Logic
  - [ ] 4.1 Crear migración Flyway V4 (simulations, simulation_share_tokens)
    - `V4__create_simulations.sql`: tabla `simulations` con todos los campos del diseño de BD, índices compuestos `idx_simulations_user_status(user_id, status)`, `idx_simulations_created_at`
    - Tabla `simulation_share_tokens`: id, simulation_id FK, token UNIQUE, expires_at, created_at; índices `idx_share_token`, `idx_share_expires`
    - _Requirements: 6.1, 11.1_

  - [ ] 4.2 Implementar `Simulation` aggregate root con Builder pattern y validación de invariantes
    - `Simulation` en `simulation_service/domain/model/` con todos los campos del modelo de dominio
    - Builder estático interno con validación en `build()`: capacityKw ∈ [1, 10000], initialInvestment > 0, location válida
    - Métodos: `completeCalculation(SimulationResults)`, `archive()`, `clone(Long userId)`, `transitionTo(SimulationStatus)`
    - Sin anotaciones Spring
    - _Requirements: 6.1, 6.8, 6.9, 6.10, 7.7_

  - [ ] 4.3 Implementar `SimulationStatus` enum + máquina de estados
    - `SimulationStatus` enum: DRAFT, COMPLETED, ARCHIVED
    - `transitionTo(SimulationStatus next)` en `Simulation`: validar transiciones permitidas (DRAFT→COMPLETED, DRAFT→ARCHIVED, COMPLETED→ARCHIVED); lanzar `InvalidStateTransitionException` para transiciones inválidas
    - _Requirements: 7.5, 7.7_

  - [ ] 4.4 Implementar value objects: `EnergyData`, `SimulationResults`
    - `EnergyData` record: `double kwhPerYear`, `double[] monthlyBreakdown` (12 valores)
    - `SimulationResults` record: energyGeneratedAnnual, roiPercentage, paybackYears, co2ReductionAnnual, npvValue, irrPercentage
    - Sin anotaciones Spring
    - _Requirements: 6.2, 6.3, 6.4, 6.5, 6.6, 6.7_

  - [ ] 4.5 Implementar `ClimateFactorStrategy` interface + estrategias por tipo de energía
    - `ClimateFactorStrategy` interfaz: `double calculate(ClimateData data)`
    - `SolarClimateStrategy`: factor = avgSolarIrradiation / 5.0 (normalizado a irradiación media global)
    - `WindClimateStrategy`: factor = (avgWindSpeed / 7.0)^3 (ley cúbica del viento)
    - `HydroClimateStrategy`: factor constante 0.85 (factor de planta típico)
    - `ClimateFactorStrategyFactory`: retornar estrategia según `EnergyType`
    - _Requirements: 6.2_

  - [ ] 4.6 Implementar `ROICalculator` domain service (puro, sin Spring)
    - `calculate(double totalRevenue, double initialInvestment)` → `double roiPercentage`
    - Fórmula: `((totalRevenue - initialInvestment) / initialInvestment) * 100`
    - Lanzar `IllegalArgumentException` si initialInvestment ≤ 0
    - _Requirements: 6.3_

  - [ ] 4.7 Implementar `CO2Calculator` domain service
    - `calculate(double energyGeneratedKwh, double emissionFactor)` → `double co2ReductionTons`
    - Fórmula: `energyGeneratedKwh * emissionFactor / 1000`
    - Factor de emisión por defecto: 0.5 kg CO₂/kWh
    - _Requirements: 6.5_

  - [ ] 4.8 Implementar `NPVCalculator` domain service
    - `calculate(double[] annualCashFlows, double discountRate, double initialInvestment)` → `double npv`
    - Fórmula: `Σ [cashFlow_t / (1 + discountRate)^t] - initialInvestment`
    - Validar discountRate > -1 para evitar división por cero
    - _Requirements: 6.6_

  - [ ] 4.9 Implementar `IRRCalculator` domain service (Newton-Raphson iterativo, tolerancia 1e-7)
    - `calculate(double[] cashFlows, double initialInvestment)` → `double irr`
    - Algoritmo Newton-Raphson: máx 1000 iteraciones, tolerancia 1e-7
    - Valor inicial: 0.1 (10%)
    - Retornar `Double.NaN` si no converge
    - _Requirements: 6.7_

  - [ ] 4.10 Implementar `SimulationEngine` domain service (orquesta todos los calculators)
    - `SimulationEngine` en `simulation_service/domain/service/`
    - `calculate(SimulationInput input, Technology technology)` → `SimulationResults`
    - Flujo: calcular climateFactor → energyGenerated → annualSavings → ROI → payback → CO₂ → NPV → IRR → monthlyBreakdown (distribución uniforme)
    - Sin anotaciones Spring (inyección por constructor en tests)
    - _Requirements: 6.2, 6.3, 6.4, 6.5, 6.6, 6.7_

  - [ ] 4.11 Implementar `SimulationInput` value object (parámetros de entrada al motor)
    - `SimulationInput` record: capacityKw, efficiency, hoursPerYear (8760), climateFactor, initialInvestment, electricityTariff, currentConsumptionKwhYear, lifespanYears, discountRate (default 0.08)
    - _Requirements: 6.1_

  - [ ]* 4.12 Tests unitarios del motor: ROICalculator, CO2Calculator, NPVCalculator, IRRCalculator
    - `ROICalculatorTest`: caso base, ROI negativo (pérdida), initialInvestment=0 → excepción
    - `CO2CalculatorTest`: caso base con factor 0.5, factor personalizado
    - `NPVCalculatorTest`: flujos positivos → NPV positivo, tasa alta → NPV negativo
    - `IRRCalculatorTest`: proyecto viable → IRR > 0, proyecto inviable → NaN o negativo
    - Cobertura objetivo ≥80% en `simulation_service/domain/service/`
    - _Requirements: 6.2–6.7_

  - [ ]* 4.13 Tests property-based con jqwik (Propiedades 9–16)
    - **Propiedad 9: Energía generada siempre positiva**
      - `@Property(tries=200)` con `@ForAll @DoubleRange(min=1, max=10000) capacityKw`, efficiency, climateFactor → `energyGenerated > 0`
      - **Validates: Requirements 6.2**
    - **Propiedad 10: Monotonicidad de energía respecto a capacidad**
      - Duplicar capacidad → duplicar energía (±0.1% tolerancia)
      - **Validates: Requirements 6.2**
    - **Propiedad 11: CO₂ siempre positivo**
      - Para cualquier energyGenerated > 0 y emissionFactor > 0 → co2Reduction > 0
      - **Validates: Requirements 6.5**
    - **Propiedad 12: Payback siempre positivo**
      - Para initialInvestment > 0 y annualSavings > 0 → paybackYears > 0
      - **Validates: Requirements 6.4**
    - **Propiedad 13: IRR round-trip con VAN**
      - `npv(cashFlows, irr, investment) ≈ 0` (tolerancia 0.01) para proyectos viables
      - **Validates: Requirements 6.6, 6.7**
    - **Propiedad 14: VAN positivo para proyectos rentables**
      - Si `annualSavings * lifespanYears > initialInvestment` → NPV > 0 con tasa 0%
      - **Validates: Requirements 6.6**
    - **Propiedad 15: Validación de parámetros de entrada**
      - capacityKw fuera de [1, 10000] → `IllegalArgumentException`
      - initialInvestment ≤ 0 → `IllegalArgumentException`
      - **Validates: Requirements 6.8, 6.9**
    - **Propiedad 16: Máquina de estados — transiciones inválidas siempre lanzan excepción**
      - Para cualquier estado en {COMPLETED, ARCHIVED} → transitionTo(DRAFT) lanza `InvalidStateTransitionException`
      - **Validates: Requirements 7.7**

- [ ] 4.14 Checkpoint — Fase 4 completa
  - Asegurar que todos los tests del motor pasan, incluyendo PBT
  - Verificar que `SimulationEngine` no tiene ninguna anotación Spring
  - Preguntar al usuario si hay dudas antes de continuar

---

## Fase 5: Simulation Service — CRUD & Lifecycle

- [ ] 5. Fase 5: Simulation Service — CRUD & Lifecycle
  - [ ] 5.1 Implementar `SimulationRepository` port + JPA adapter (con paginación y filtros)
    - Port: `save(Simulation)`, `findById(Long)`, `findByUserIdAndStatusNot(Long userId, SimulationStatus status, Pageable)`, `findAllByUserId(Long userId, Pageable)`, `findByIds(List<Long>)`
    - `SimulationEntity` con anotaciones JPA, `@ManyToOne` a `TechnologyEntity`
    - `SimulationMapper` MapStruct para conversión bidireccional
    - _Requirements: 7.1, 7.3_

  - [ ] 5.2 Implementar `CreateSimulationCommand` + `CreateSimulationUseCase`
    - `CreateSimulationCommand` record: userId, technologyId, name, location, capacityKw, initialInvestment, electricityTariff, currentConsumptionKwhYear, climateData
    - `CreateSimulationUseCase` interfaz en `application/port/in/`
    - `CreateSimulationService`: cargar Technology, construir SimulationInput, ejecutar SimulationEngine, crear Simulation con `completeCalculation()`, persistir
    - _Requirements: 6.1, 6.2_

  - [ ] 5.3 Implementar `POST /api/v1/simulations` (crea + calcula + retorna COMPLETED)
    - `SimulationRequestDTO` con validaciones: `@NotBlank name`, `@DecimalMin("1") @DecimalMax("10000") capacityKw`, `@Positive initialInvestment`, location con lat/lon validados
    - `SimulationResponseDTO` con todos los campos de resultado
    - Controller: extraer userId del SecurityContext, delegar a use case, retornar HTTP 201
    - _Requirements: 6.1, 6.8, 6.9, 6.10, 6.11_

  - [ ] 5.4 Implementar `GET /api/v1/simulations/{id}` (solo owner o ADMIN)
    - Cargar simulación por id; HTTP 404 si no existe
    - Verificar que `simulation.userId == authenticatedUserId` o usuario tiene rol ADMIN; HTTP 403 si no
    - Retornar `SimulationResponseDTO` completo
    - _Requirements: 7.1, 7.2_

  - [ ] 5.5 Implementar `GET /api/v1/simulations/my-simulations` (paginado, filtro por status)
    - Parámetros: `?page=0&size=20&sort=createdAt,desc&status=COMPLETED`
    - Retornar `Page<SimulationSummaryDTO>` (id, name, status, technologyName, energyGeneratedAnnual, roiPercentage, createdAt)
    - _Requirements: 7.3_

  - [ ] 5.6 Implementar `PUT /api/v1/simulations/{id}` (solo DRAFT, recalcula, incrementa version)
    - Verificar ownership y estado DRAFT; HTTP 409 si COMPLETED o ARCHIVED
    - Actualizar parámetros, re-ejecutar SimulationEngine, incrementar `version`
    - _Requirements: 7.4, 7.5_

  - [ ] 5.7 Implementar `DELETE /api/v1/simulations/{id}` (soft delete → ARCHIVED)
    - Verificar ownership; llamar `simulation.archive()`; persistir; HTTP 204
    - _Requirements: 7.6_

  - [ ] 5.8 Implementar `POST /api/v1/simulations/{id}/clone`
    - Cargar simulación origen, verificar ownership
    - Llamar `simulation.clone(userId)` → nueva Simulation en DRAFT con mismos parámetros, version=1
    - Persistir y retornar nuevo id con HTTP 201
    - _Requirements: 7.8_

  - [ ] 5.9 Implementar `POST /api/v1/simulations/from-scenario/{scenarioId}`
    - Cargar Scenario por id; HTTP 404 si no existe
    - Construir `CreateSimulationCommand` con parámetros del escenario
    - Ejecutar `CreateSimulationUseCase`; retornar HTTP 201
    - _Requirements: 8.4_

  - [ ] 5.10 Implementar `GET /api/v1/simulations/{id}/report` (JSON + PDF con Accept header)
    - Verificar ownership y estado COMPLETED; HTTP 409 si DRAFT o ARCHIVED
    - Si `Accept: application/json` → retornar `SimulationReportDTO` con todos los campos + desglose mensual
    - Si `Accept: application/pdf` → generar PDF con iText 7 y retornar como `ResponseEntity<byte[]>`
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [ ]* 5.11 Tests de integración con Testcontainers: flujo completo crear → leer → actualizar → archivar
    - `SimulationLifecycleIntegrationTest`: crear simulación → GET → PUT (recalcular) → DELETE (archivar)
    - Verificar que resultados calculados son persistidos correctamente
    - Verificar que version se incrementa en cada PUT
    - _Requirements: 6.1, 7.1, 7.4, 7.6_

  - [ ]* 5.12 Tests de seguridad: 401 sin JWT, 403 acceso a simulación ajena, 409 actualizar COMPLETED
    - `SimulationSecurityTest` con `@WebMvcTest` + `@MockBean`
    - Sin JWT → 401 en GET /simulations/1
    - JWT de usuario B intentando GET /simulations/{id de usuario A} → 403
    - PUT sobre simulación COMPLETED → 409 con errorCode INVALID_STATE_TRANSITION
    - _Requirements: 7.2, 7.5, 18.1_

- [ ] 5.13 Checkpoint — Fase 5 completa
  - Asegurar que todos los tests pasan
  - Verificar flujo completo de simulación con Swagger UI
  - Preguntar al usuario si hay dudas antes de continuar

---

## Fase 6: Simulation Advanced Features

- [ ] 6. Fase 6: Simulation Advanced Features
  - [ ] 6.1 Implementar `POST /api/v1/simulations/compare` (2–5 IDs, retorna bestValues)
    - `CompareRequestDTO` record: `@Size(min=2, max=5) List<Long> simulationIds`
    - Verificar ownership de cada simulación; HTTP 403 si alguna no pertenece al usuario
    - Calcular `bestValues`: highestRoi, lowestPayback, highestNpv, highestCo2
    - Retornar `ComparisonResponseDTO` con lista de simulaciones + bestValues
    - _Requirements: 10.1, 10.2, 10.3_

  - [ ] 6.2 Implementar `POST /api/v1/simulations/{id}/share` (genera token UUID, expiración 30 días)
    - Verificar ownership; generar `UUID.randomUUID().toString()` como token
    - Persistir en `simulation_share_tokens` con `expires_at = now + 30 days`
    - Retornar `{ shareUrl: "/api/v1/shared/{token}", expiresAt }`
    - _Requirements: 11.1, 11.4_

  - [ ] 6.3 Implementar `GET /api/v1/shared/{token}` (vista pública sin auth)
    - Buscar token en `simulation_share_tokens`; HTTP 404 si no existe o expirado
    - Retornar `SimulationPublicDTO` (sin datos del propietario) en modo solo lectura
    - Endpoint en `permitAll()` en SecurityConfig
    - _Requirements: 11.2, 11.3_

  - [ ] 6.4 Implementar `GET /api/v1/dashboard` (métricas agregadas del usuario)
    - Query agregada: COUNT simulaciones, SUM energyGeneratedAnnual, SUM co2ReductionAnnual, SUM (energyGeneratedAnnual * electricityTariff)
    - Incluir lista de 5 simulaciones más recientes (no archivadas)
    - _Requirements: 12.1_

  - [ ] 6.5 Implementar `GET /api/v1/simulations/{id}/energy-chart` (desglose mensual 12 meses)
    - Retornar `EnergyChartDTO`: lista de 12 objetos `{ month: "Enero", energyKwh: 660.0 }`
    - Calcular distribución mensual desde `EnergyData.monthlyBreakdown`
    - _Requirements: 12.2_

  - [ ] 6.6 Implementar `GET /api/v1/simulations/map-data` (coordenadas + metadata para mapa)
    - Retornar lista de `MapDataDTO`: id, name, latitude, longitude, status, roiPercentage, technologyName
    - Solo simulaciones no archivadas del usuario autenticado
    - _Requirements: 12.3_

  - [ ] 6.7 Implementar generación de PDF con iText 7 (reporte técnico-económico-ambiental)
    - `PdfReportGenerator` en `simulation_service/infrastructure/`
    - Secciones: portada (nombre, fecha), parámetros de entrada, resultados calculados (tabla), desglose mensual (tabla), resumen ambiental
    - Fuente: Helvetica, colores corporativos, logo placeholder
    - _Requirements: 9.3, 9.4_

  - [ ] 6.8 Implementar job `@Scheduled` para limpiar OTPs y tokens expirados
    - `CleanupScheduledJob` en `shared/infrastructure/`
    - `@Scheduled(cron = "0 0 2 * * *")` (2 AM diario)
    - Eliminar: `otp_codes` donde `expires_at < NOW()`, `token_blacklist` donde `expires_at < NOW()`, `refresh_tokens` donde `expires_at < NOW() AND revoked = TRUE`, `simulation_share_tokens` donde `expires_at < NOW()`
    - _Requirements: 2.14_

  - [ ]* 6.9 Tests de integración para endpoints de Fase 6
    - `CompareSimulationsIntegrationTest`: comparar 3 simulaciones propias → 200 con bestValues; incluir simulación ajena → 403; 1 ID → 400
    - `ShareSimulationIntegrationTest`: generar token → GET /shared/{token} sin auth → 200; token expirado → 404
    - `DashboardIntegrationTest`: usuario con 3 simulaciones → métricas correctas
    - _Requirements: 10.1, 11.2, 12.1_

- [ ] 6.10 Checkpoint — Fase 6 completa
  - Asegurar que todos los tests pasan
  - Verificar generación de PDF manualmente
  - Preguntar al usuario si hay dudas antes de continuar

---

## Fase 7: AI Service Integration

- [ ] 7. Fase 7: AI Service Integration
  - [ ] 7.1 Implementar `LLMProviderPort` interface (domain)
    - `LLMProviderPort` en `ai_service/domain/port/out/`
    - Método: `AIResponse complete(AIRequest request)`
    - Sin anotaciones Spring
    - _Requirements: 13.3_

  - [ ] 7.2 Implementar `AIRequest` + `AIResponse` value objects
    - `AIRequest` record: model, systemPrompt, List<Message> messages, maxTokens, temperature
    - `AIResponse` record: content, tokensUsed, finishReason
    - `Message` record: role (String), content (String)
    - _Requirements: 13.1, 15.1_

  - [ ] 7.3 Implementar `OpenAIAdapter` (infrastructure, llama GPT-4o)
    - `OpenAIAdapter implements LLMProviderPort`
    - Usar `RestClient` (Spring 6) para llamar `https://api.openai.com/v1/chat/completions`
    - Mapear `AIRequest` → OpenAI request body; mapear respuesta → `AIResponse`
    - Configurar API key desde properties `ai.openai.api-key`
    - `@PostConstruct` validar que `ai.openai.api-key` no está vacío ni es el valor placeholder `sk-...`; lanzar `IllegalStateException("OpenAI API key is not configured")` si falta — impide arranque con clave inválida
    - _Requirements: 13.1_

  - [ ] 7.4 Implementar `AnthropicAdapter` (infrastructure, fallback Claude 3.5 Sonnet)
    - `AnthropicAdapter implements LLMProviderPort`
    - Llamar `https://api.anthropic.com/v1/messages` con header `anthropic-version`
    - Configurar API key desde properties `ai.anthropic.api-key`
    - `@PostConstruct` validar que `ai.anthropic.api-key` no está vacío ni es el valor placeholder `sk-ant-...`; lanzar `IllegalStateException("Anthropic API key is not configured")` si falta
    - _Requirements: 13.3_

  - [ ] 7.5 Implementar `PromptBuilderService` (domain service, construye prompts con contexto)
    - `buildSuggestConfigurationPrompt(Location, double consumption, double budget)` → String systemPrompt
    - `buildPredictPerformancePrompt(Simulation, int horizonYears)` → String systemPrompt
    - `buildGenerateReportPrompt(Simulation, String locale)` → String systemPrompt
    - `buildChatSystemPrompt(Long userId)` → String systemPrompt con contexto del usuario
    - Sin anotaciones Spring
    - _Requirements: 13.2, 14.2, 16.2_

  - [ ] 7.6 Implementar `ResponseParserService` (parsea JSON de respuestas LLM)
    - `parseRecommendations(String content)` → `List<ConfigurationRecommendationDTO>`
    - `parsePredictions(String content)` → `List<YearlyPredictionDTO>`
    - Usar `ObjectMapper` con manejo de errores (retornar lista vacía si JSON inválido)
    - _Requirements: 13.1, 14.1_

  - [ ] 7.7 Implementar `ChatSession` aggregate + `ChatMessage` entity
    - `ChatSession`: id, userId, sessionId (UUID), messages (List<ChatMessage>), lastActive
    - `addMessage(MessageRole role, String content, int tokensUsed)` → actualizar lastActive
    - `ChatMessage`: id, sessionId, role (MessageRole enum), content, tokensUsed, createdAt
    - Sin anotaciones Spring
    - _Requirements: 15.2_

  - [ ] 7.8 Implementar `ChatSessionRepository` port + JPA adapter
    - Port: `save(ChatSession)`, `findBySessionId(String)`, `findByUserId(Long)`
    - `ChatSessionEntity` + `ChatMessageEntity` con `@OneToMany(cascade=ALL)`
    - _Requirements: 15.2_

  - [ ] 7.9 Implementar `POST /api/v1/ai/chat` (conversacional con historial de sesión)
    - `ChatRequestDTO` record: sessionId (nullable, genera nuevo si null), message
    - Cargar o crear ChatSession, añadir mensaje USER, construir historial para LLM (últimos 10 mensajes), llamar LLM, añadir respuesta ASSISTANT, persistir sesión
    - Retornar `ChatResponseDTO`: sessionId, role, message, timestamp, tokensUsed
    - _Requirements: 15.1, 15.2, 15.3, 15.4_

  - [ ] 7.10 Implementar `POST /api/v1/ai/suggest-configuration` (3 recomendaciones rankeadas)
    - `SuggestConfigRequestDTO`: location, annualConsumptionKwh, budget, electricityTariff
    - Construir prompt con `PromptBuilderService`, llamar LLM, parsear respuesta con `ResponseParserService`
    - Retornar `List<ConfigurationRecommendationDTO>` con rank, technology, capacityKw, estimatedInvestment, expectedRoi, justification
    - _Requirements: 13.1, 13.2_

  - [ ] 7.11 Implementar `POST /api/v1/ai/predict-performance/{id}` (proyecciones año a año con degradación)
    - Cargar simulación, verificar ownership
    - Construir prompt con horizonte temporal y factor de degradación por EnergyType (SOLAR: 0.5%/año, WIND: 0.3%/año, HYDRO: 0.1%/año)
    - Retornar `List<YearlyPredictionDTO>`: year, energyKwh, cumulativeSavings, cumulativeCo2
    - _Requirements: 14.1, 14.2, 14.3_

  - [ ] 7.12 Implementar `POST /api/v1/ai/generate-report/{id}` (narrativa en idioma del usuario)
    - Verificar que simulación está COMPLETED; HTTP 409 si DRAFT
    - Construir prompt con locale del usuario (header `Accept-Language` o perfil)
    - Retornar `GeneratedReportDTO`: title, executiveSummary, energyAnalysis, financialAnalysis, environmentalImpact, conclusion
    - _Requirements: 16.1, 16.2, 16.3_

  - [ ] 7.13 Implementar circuit breaker Resilience4j (fallback OpenAI → Anthropic → HTTP 503)
    - `AIApplicationService` con `@CircuitBreaker(name="openai", fallbackMethod="fallbackToAnthropic")`
    - `fallbackToAnthropic()` llama `AnthropicAdapter`
    - `fallbackToError()` lanza `AIServiceUnavailableException` → GlobalExceptionHandler → HTTP 503
    - Configurar en `application.yml`: slidingWindowSize=10, failureRateThreshold=50, waitDurationInOpenState=30s
    - _Requirements: 13.3_

  - [ ] 7.14 Implementar retry con backoff exponencial para llamadas a LLMs y email
    - `@Retry(name="llm")` en `OpenAIAdapter.complete()` y `AnthropicAdapter.complete()`
    - `@Retry(name="email")` en `SendGridEmailAdapter`
    - Configurar: maxAttempts=3, waitDuration=1s, multiplier=2 (1s → 2s → 4s)
    - _Requirements: 13.3_

  - [ ]* 7.16 Tests con WireMock: mockear OpenAI y Anthropic, testear fallback y circuit breaker
    - `AIServiceIntegrationTest` con `WireMockServer`
    - OpenAI responde 200 → respuesta correcta parseada
    - OpenAI responde 500 → fallback a Anthropic → respuesta correcta
    - OpenAI + Anthropic responden 500 → HTTP 503 con errorCode AI_SERVICE_UNAVAILABLE
    - Circuit breaker abierto tras 5 fallos → llamadas directas a fallback sin intentar OpenAI
    - _Requirements: 13.3_

- [ ] 7.17 Checkpoint — Fase 7 completa
  - Asegurar que todos los tests pasan
  - Verificar circuit breaker con WireMock en tests
  - Preguntar al usuario si hay dudas antes de continuar

---

## Fase 8: Frontend Complete

- [ ] 8. Fase 8: Frontend Complete
  - [ ] 8.1 Inicializar proyecto Vite + React 18 + TypeScript (strict mode) con path aliases
    - `npm create vite@latest frontend -- --template react-ts`
    - Configurar `tsconfig.json`: `"strict": true`, `"baseUrl": "src"`, paths `@/*` → `src/*`
    - Configurar `vite.config.ts` con `resolve.alias` para `@/`
    - Estructura de carpetas: `src/{pages,components,hooks,stores,services,types}`
    - _Requirements: (frontend stack)_

  - [ ] 8.2 Configurar Tailwind CSS + shadcn/ui + Radix UI
    - `npm install -D tailwindcss postcss autoprefixer` + `npx tailwindcss init -p`
    - `npx shadcn-ui@latest init` con tema neutral, CSS variables
    - Instalar componentes shadcn: Button, Input, Card, Table, Dialog, Badge, Skeleton, Select
    - _Requirements: (frontend stack)_

  - [ ] 8.3 Configurar TanStack Query v5 (QueryClient, DevTools) + Zustand
    - `npm install @tanstack/react-query @tanstack/react-query-devtools zustand`
    - `QueryClient` con `defaultOptions`: staleTime 5 min, retry 2
    - `QueryClientProvider` + `ReactQueryDevtools` en `main.tsx`
    - _Requirements: (frontend stack)_

  - [ ] 8.4 Implementar `authStore` (Zustand): accessToken en memoria, clearAuth
    - `src/stores/authStore.ts`
    - State: `accessToken: string | null`, `user: UserProfile | null`, `isAuthenticated: boolean`
    - Actions: `setTokens(accessToken, user)`, `clearAuth()`
    - Access token NUNCA en localStorage (solo en memoria Zustand)
    - _Requirements: 18.4 (seguridad XSS)_

  - [ ] 8.5 Implementar `uiStore` (Zustand): locale, isChatOpen
    - `src/stores/uiStore.ts`
    - State: `locale: 'es' | 'en'`, `isChatOpen: boolean`
    - Actions: `setLocale(locale)`, `toggleChat()`
    - _Requirements: (i18n, chat widget)_

  - [ ] 8.6 Implementar `httpClient` (Axios): interceptores JWT + refresh automático con cola de requests
    - `src/services/httpClient.ts`
    - Request interceptor: inyectar `Authorization: Bearer {accessToken}` desde authStore
    - Response interceptor: 401 → llamar `/auth/refresh` (cookie HttpOnly) → reintentar request original
    - Cola de requests pendientes durante refresh para evitar múltiples llamadas simultáneas
    - `withCredentials: true` para enviar cookie de refresh token
    - _Requirements: 2.9_

  - [ ] 8.7 Implementar `authService`, `simulationService`, `technologyService`, `aiService`
    - `authService`: `login1(email, password)`, `login2(email, otp)`, `logout()`, `getMe()`
    - `simulationService`: `create(dto)`, `getById(id)`, `getMySimulations(params)`, `update(id, dto)`, `delete(id)`, `clone(id)`, `compare(ids)`, `share(id)`, `getReport(id, format)`, `getDashboard()`, `getEnergyChart(id)`, `getMapData()`
    - `technologyService`: `getAll(params)`, `getById(id)`
    - `aiService`: `chat(sessionId, message)`, `suggestConfiguration(dto)`, `predictPerformance(id, years)`, `generateReport(id)`
    - _Requirements: (todos los endpoints)_

  - [ ] 8.8 Implementar `LoginPage`: CredentialsForm (paso 1) + OtpForm (paso 2 con contador regresivo)
    - `CredentialsForm`: React Hook Form + Zod (`@email`, `@min(8)` password), submit → `authService.login1()` → mostrar OtpForm
    - `OtpForm`: input de 6 dígitos, contador regresivo 5:00 min, botón "Reenviar" (deshabilitado hasta 0), submit → `authService.login2()` → redirect a `/`
    - Manejo de errores: 401 → mensaje inline, 429 → mostrar tiempo de espera
    - _Requirements: 2.1, 2.4, 2.7_

  - [ ] 8.9 Implementar `ProtectedRoute` + React Router v6 con todas las rutas
    - `ProtectedRoute`: si `!isAuthenticated` → redirect a `/login`
    - Rutas: `/login`, `/shared/:token` (pública), `/` (Dashboard), `/simulations/new`, `/simulations/:id`, `/simulations/compare`
    - `NotFoundPage` para rutas no definidas
    - _Requirements: 18.1_

  - [ ] 8.10 Implementar `DashboardPage`: métricas skeleton + tabla simulaciones recientes
    - TanStack Query: `useQuery(['dashboard'])` → `getDashboard()`
    - Mientras carga: `SkeletonCard` x4 para métricas
    - Métricas: totalSimulations, totalEnergyKwh, totalCo2Tons, totalSavingsUsd
    - Tabla de simulaciones recientes con columnas: nombre, tecnología, ROI, estado, fecha
    - Estado vacío: ilustración + "Crea tu primera simulación"
    - _Requirements: 12.1, 12.4_

  - [ ] 8.11 Implementar `MapView` component (Leaflet + react-leaflet, marcadores con popups)
    - `npm install leaflet react-leaflet @types/leaflet`
    - `MapContainer` con `TileLayer` OpenStreetMap
    - `Marker` por cada simulación con `Popup`: nombre, ROI, tecnología, link a detalle
    - Color de marcador según EnergyType (solar=amarillo, wind=azul, hydro=verde)
    - _Requirements: 12.3_

  - [ ] 8.12 Implementar `SimulationCreatePage`: formulario React Hook Form + Zod + selector tecnología + mapa Leaflet
    - `simulationSchema` Zod con todas las validaciones del backend
    - Selector de tecnología: `useQuery(['technologies'])` → dropdown con nombre y tipo
    - Mapa Leaflet interactivo: click en mapa → actualizar `location.latitude/longitude` en formulario
    - Submit → `simulationService.create(dto)` → redirect a `/simulations/{id}`
    - _Requirements: 6.1, 6.8, 6.9, 6.10_

  - [ ] 8.13 Implementar `SimulationDetailPage`: tarjetas métricas + EnergyChart + CashFlowChart
    - `useQuery(['simulation', id])` → `simulationService.getById(id)`
    - Tarjetas: energía anual (kWh), ROI (%), payback (años), CO₂ (ton/año), VAN ($), TIR (%)
    - `EnergyChart`: Recharts `AreaChart` con datos mensuales de `/energy-chart`
    - `CashFlowChart`: Recharts `LineChart` con flujos acumulados + línea de payback
    - Botones: Clonar, Compartir, Generar PDF, Comparar
    - _Requirements: 9.1, 12.2_

  - [ ] 8.14 Implementar `SimulationComparePage`: tabla comparativa + RadarChart + highlight mejor valor
    - Selección de simulaciones: multi-select (2–5) desde lista de "mis simulaciones"
    - `useMutation` → `simulationService.compare(ids)`
    - Tabla comparativa: fila por métrica, columna por simulación, celda con mejor valor resaltada en verde
    - `RadarChart` Recharts con métricas normalizadas (ROI, payback inverso, NPV, CO₂)
    - _Requirements: 10.1, 10.3_

  - [ ] 8.15 Implementar `ChatWidget`: widget flotante + ReactMarkdown + rehype-sanitize + sugerencias rápidas
    - Botón flotante `fixed bottom-4 right-4` con `toggleChat()` del uiStore
    - Panel: historial de mensajes, input de texto, botón enviar
    - `ReactMarkdown` con `rehypeSanitize` para renderizar respuestas del asistente
    - Sugerencias rápidas cuando historial vacío: "¿Qué tecnología me recomiendas?", "Explícame el VAN", "¿Cómo mejorar mi ROI?"
    - `useMutation` → `aiService.chat(sessionId, message)`
    - _Requirements: 15.1, 15.2, 15.3_

  - [ ] 8.16 Implementar i18n (ES/EN) con react-i18next, cambio de idioma sin recarga
    - `npm install react-i18next i18next`
    - Archivos `src/locales/es.json` y `src/locales/en.json` con todas las cadenas de UI
    - `i18n.changeLanguage(locale)` desde uiStore `setLocale()`
    - Selector de idioma en navbar
    - _Requirements: 16.1 (idioma del usuario)_

  - [ ]* 8.17 Tests Vitest + Testing Library: LoginPage, SimulationForm, ProtectedRoute
    - `LoginPage.test.tsx`: renderizar → completar paso 1 → mock authService.login1 → mostrar OtpForm → completar OTP → mock login2 → verificar redirect
    - `SimulationForm.test.tsx`: validación Zod — capacityKw=0 → error, lat=200 → error, submit válido → llamar simulationService.create
    - `ProtectedRoute.test.tsx`: sin auth → redirect /login; con auth → renderizar children
    - _Requirements: 6.8, 6.9, 6.10_

  - [ ]* 8.18 Tests property-based fast-check: serialización SimulationRequestDTO round-trip (Propiedad 17)
    - `npm install -D fast-check`
    - **Propiedad 17: SimulationRequestDTO serialización round-trip**
    - `fc.record({ name, capacityKw: fc.double({min:1, max:10000}), initialInvestment: fc.double({min:0.01}), latitude: fc.double({min:-90, max:90}), longitude: fc.double({min:-180, max:180}) })`
    - `JSON.parse(JSON.stringify(dto))` → todos los campos preservados con precisión ≥8 decimales
    - **Validates: Requirements 6.1**

  - [ ] 8.19 Dockerfile frontend multi-stage + nginx.conf SPA
    - `frontend/Dockerfile`: stage build `node:20-alpine` (`npm ci` → `npm run build`) + stage serve `nginx:1.25-alpine`
    - `frontend/nginx.conf`: gzip, headers de seguridad (X-Frame-Options DENY, X-Content-Type-Options), cache de assets estáticos 1 año, SPA fallback `try_files $uri /index.html`, proxy `/api/` → `backend:8080`
    - _Requirements: (deployment)_

  - [ ] 8.20 Actualizar `docker-compose.yml` con servicio frontend
    - Añadir servicio `frontend`: build context `./frontend`, ports `80:80`, depends_on backend, networks renewsim-network
    - Verificar que `docker compose up` levanta backend + frontend + db correctamente
    - _Requirements: (deployment)_

- [ ] 8.21 Checkpoint final — Plataforma completa
  - Asegurar que todos los tests backend y frontend pasan
  - Verificar `docker compose up` levanta los 3 servicios sin errores
  - Verificar flujo completo: registro → activación → login 2FA → crear simulación → ver dashboard → chat IA
  - Preguntar al usuario si hay dudas antes de entregar

---

## Resumen de Fases

| Fase | Descripción | Duración | Hito de Entrega |
|------|-------------|----------|-----------------|
| 1 | Backend Foundation | 3 días | 28 mar — Proyecto compilable, migraciones V1-V2, CI verde |
| 2 | User & Auth Service | 4 días | 1 abr — Login 2FA funcional, JWT, rate limiting |
| 3 | Technology & Scenario Service | 2 días | 3 abr — Catálogo con caché, seeds en BD |
| 4 | Simulation Engine & Core Logic | 5 días | 8 abr — Motor de cálculo con PBT verde |
| 5 | Simulation Service CRUD & Lifecycle | 3 días | 11 abr — CRUD completo, PDF, máquina de estados |
| 6 | Simulation Advanced Features | 2 días | 13 abr — Comparación, compartir, dashboard, mapa |
| 7 | AI Service Integration | 3 días | 13 abr — Chat IA, sugerencias, circuit breaker |
| 8 | Frontend Complete | 5 días | 15 abr — SPA completa, Docker, CI/CD verde |

---

## Definition of Done (DoD) por Fase

- [ ] Todos los tests unitarios pasan (`./mvnw clean test` o `npm run test -- --run`)
- [ ] Cobertura JaCoCo ≥70% en bounded contexts afectados (`./mvnw jacoco:check`)
- [ ] Sin errores de compilación ni warnings críticos
- [ ] Endpoints documentados en Swagger UI (`/swagger-ui.html`)
- [ ] Migraciones Flyway aplicadas sin errores en perfil `docker`
- [ ] `docker compose up` levanta todos los servicios healthy
- [ ] PR revisado y mergeado a `develop`

---

## Riesgos Técnicos

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|------------|
| IRRCalculator no converge para proyectos extremos | Media | Alto | Retornar `Double.NaN`, documentar en API, cubrir con PBT Propiedad 13 |
| OpenAI API rate limits en desarrollo | Alta | Medio | WireMock en tests, AnthropicAdapter como fallback, circuit breaker Resilience4j |
| Testcontainers lento en CI (MySQL startup) | Media | Medio | `@Container static` para reusar contenedor entre tests, `start-period=60s` en healthcheck |
| iText 7 licencia AGPL en producción | Baja | Alto | Verificar licencia antes de Fase 5; alternativa: OpenPDF (fork LGPL) |
| Caffeine cache inconsistente en escalado horizontal | Baja | Medio | Documentado en ADR-006; Redis en roadmap si > 2 instancias |
| fast-check genera valores NaN/Infinity en PBT frontend | Media | Bajo | Usar `fc.double({ noNaN: true, noDefaultInfinity: true })` en todos los arbitrarios |
| Leaflet SSR incompatible si se migra a Next.js | Baja | Bajo | Importación dinámica con `ssr: false`; no aplica en Vite SPA actual |

---

> Notas:
> - Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido.
> - Cada tarea referencia los requerimientos específicos que valida.
> - Los checkpoints son puntos de sincronización obligatorios antes de avanzar a la siguiente fase.
> - Las Propiedades 9–17 corresponden a las propiedades de corrección definidas en `design/08-testing-strategy.md`.
