# RenewSim - Renewable Energy Simulation Platform 🌞⚡💧

![Status](https://img.shields.io/badge/status-in%20development-yellow)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen?logo=springboot)
![React](https://img.shields.io/badge/React-18-blue?logo=react)
![Coverage](https://img.shields.io/badge/coverage-0%25-red?logo=codecov)

[![CI - Backend](https://github.com/YOUR_USERNAME/renewsim-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/renewsim-platform/actions/workflows/ci.yml)

---

## 📋 Descripción

**RenewSim** es una plataforma profesional de simulación de proyectos de energías renovables que permite evaluar la **viabilidad técnica, económica y ambiental** de instalaciones solares, eólicas e hidroeléctricas, con análisis predictivo impulsado por **Inteligencia Artificial**.

**Proyecto de Tesis de Máster**  
**Fecha de entrega:** 15 de abril de 2026  
**Universidad:** [Tu Universidad]  
**Programa:** [Tu Programa de Máster]

---

## 🎯 Características Principales

### **💡 Simulación Energética Avanzada**
- Cálculo de generación anual de energía (kWh/año)
- ROI (Return on Investment) y periodo de retorno (payback)
- VAN (Valor Actual Neto) y TIR (Tasa Interna de Retorno)
- Reducción de emisiones de CO₂ (toneladas/año)
- Factores climáticos por tipo de energía (solar, eólica, hidráulica)

### **🔒 Seguridad Empresarial**
- Autenticación 2FA con Email OTP (6 dígitos, 5 min)
- JWT stateless (access 1h + refresh 7d en HttpOnly cookie)
- Rate limiting (5 intentos → 15 min block)
- Token blacklist para revocación inmediata (JTI)
- Roles y permisos (USER, ADMIN, ANALYST)

### **🤖 Inteligencia Artificial**
- Sugerencias de configuración basadas en ubicación y presupuesto
- Predicción de rendimiento a N años con degradación
- Generación de reportes narrativos multiidioma (ES/EN)
- Chatbot conversacional sobre energías renovables
- Fallback automático OpenAI → Anthropic (circuit breaker)

### **📊 Gestión y Colaboración**
- Dashboard con métricas agregadas y mapa interactivo
- Comparación de hasta 5 simulaciones (tabla + radar chart)
- Compartición pública con tokens de 30 días
- Generación de reportes PDF profesionales
- Escenarios predefinidos para inicio rápido

---

## 🏗️ Arquitectura

### **Stack Tecnológico**

| Capa | Tecnología | Versión |
|------|-----------|---------|
| **Backend** | Java + Spring Boot | 21 + 3.2.1 |
| **Frontend** | React + TypeScript + Vite | 18 + 5.x + 5.x |
| **Base de Datos** | MySQL | 8.0 |
| **Inteligencia Artificial** | OpenAI GPT-4o / Anthropic Claude 3.5 | Latest |
| **Cache** | Caffeine | Latest |
| **Resiliencia** | Resilience4j | Latest |
| **Testing** | jUnit 5 + jqwik + Testcontainers | Latest |
| **Despliegue** | Docker + Docker Compose | Latest |

### **Patrón Arquitectónico: Hexagonal (Ports & Adapters)**

```
┌─────────────────────────────────────────────────────┐
│                   WEB LAYER                         │
│         Controllers + DTOs + Validation             │
├─────────────────────────────────────────────────────┤
│                APPLICATION LAYER                    │
│      Use Cases + Commands + Mappers (Ports In)     │
├─────────────────────────────────────────────────────┤
│                  DOMAIN LAYER                       │
│  Aggregates + VOs + Domain Services (Pure Java)    │
├─────────────────────────────────────────────────────┤
│              INFRASTRUCTURE LAYER                   │
│  JPA Adapters + LLM Clients + Email (Ports Out)    │
└─────────────────────────────────────────────────────┘
```

### **6 Bounded Contexts (DDD)**
1. **auth_service** - Autenticación 2FA + JWT
2. **user_service** - Gestión de usuarios y perfiles
3. **role_service** - Roles y permisos
4. **technology_service** - Catálogo de tecnologías renovables
5. **simulation_service** - Motor de cálculo y CRUD de simulaciones
6. **ai_service** - Integración con LLMs (OpenAI + Anthropic)

---

## 📁 Estructura del Proyecto

```
renewsim-platform/
│
├── docs/                                # Documentación técnica
│   ├── requirements/
│   │   └── requirements.md              # 24 RF + 8 RNF
│   ├── design/
│   │   ├── 01-architecture-overview.md  # Hexagonal + 6 ADRs
│   │   ├── 02-domain-model.md           # Aggregates + VOs + Services
│   │   ├── 03-database-design.md        # ERD + migraciones Flyway
│   │   ├── 04-api-design.md             # 42 endpoints REST
│   │   ├── 05-sequence-diagrams.md      # 5 flujos principales
│   │   ├── 06-frontend-architecture.md  # React + TanStack Query
│   │   ├── 07-design-patterns.md        # 13 patrones documentados
│   │   ├── 08-testing-strategy.md       # PBT + Integration + E2E
│   │   ├── 09-security-design.md        # Amenazas + mitigaciones
│   │   └── 10-deployment-architecture.md # Docker + CI/CD
│   └── implementation/
│       └── IMPLEMENTATION_PLAN.md       # 8 fases, 123 tasks
│
├── backend/                             # Spring Boot 3.2.1 + Java 21
│   ├── src/main/java/com/renewsim/backend/
│   │   ├── auth_service/
│   │   │   ├── domain/              # OtpCode, RefreshToken (pure Java)
│   │   │   ├── application/         # LoginUseCase, commands
│   │   │   ├── infrastructure/      # JwtTokenProvider, JPA adapters
│   │   │   └── web/                 # AuthController, DTOs
│   │   ├── user_service/
│   │   │   ├── domain/              # User aggregate, UserRepository port
│   │   │   ├── application/         # RegisterUserUseCase
│   │   │   ├── infrastructure/      # UserJpaRepository adapter
│   │   │   └── web/                 # UserController
│   │   ├── role_service/
│   │   ├── technology_service/
│   │   ├── simulation_service/
│   │   │   ├── domain/              # Simulation, SimulationEngine, calculators
│   │   │   ├── application/         # CreateSimulationUseCase
│   │   │   ├── infrastructure/      # SimulationJpaRepository, PdfGenerator
│   │   │   └── web/                 # SimulationController
│   │   ├── ai_service/
│   │   │   ├── domain/              # LLMProviderPort (interface)
│   │   │   ├── application/         # ChatUseCase, SuggestConfigurationUseCase
│   │   │   ├── infrastructure/      # OpenAIAdapter, AnthropicAdapter
│   │   │   └── web/                 # AIController
│   │   └── shared/
│   │       ├── domain/              # Email, Location, Money (VOs)
│   │       ├── infrastructure/      # GlobalExceptionHandler, CacheConfig
│   │       └── web/
│   ├── src/main/resources/
│   │   ├── db/migration/            # Flyway migrations V1-V8
│   │   └── application.yml          # Perfiles: local, docker, prod
│   ├── src/test/                    # Tests unitarios + integración
│   ├── pom.xml                      # Maven dependencies
│   └── Dockerfile                   # Multi-stage build (JDK 21 → JRE 21)
│├── .github/workflows/
│   └── ci.yml                       # GitHub Actions: backend-tests + frontend-tests
│
├── docker-compose.yml               # Backend + Frontend + MySQL
├── .gitignore
└── README.md
```

---

## 🚀 Inicio Rápido

### **Prerrequisitos**

- ☕ **Java 21** (Eclipse Temurin recomendado)
- 📦 **Maven 3.9+**
- 🟢 **Node.js 20+** (para frontend)
- 🐳 **Docker + Docker Compose**
- 🗄️ **MySQL 8.0** (o via Docker)

### **Instalación**

```bash
# Clonar repositorio
git clone https://github.com/YOUR_USERNAME/renewsim-platform.git
cd renewsim-platform
```

### **Opción 1: Desarrollo Local**

#### **Backend**

```bash
cd backend

# Configurar variables de entorno (opcional)
export SPRING_PROFILES_ACTIVE=local
export JWT_SECRET=your-secret-key-minimum-32-chars-long
export DATABASE_URL=jdbc:mysql://localhost:3306/renewsim
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=password

# Ejecutar migraciones Flyway
./mvnw flyway:migrate

# Arrancar aplicación
./mvnw spring-boot:run
```

**Backend disponible en:** http://localhost:8080  
**Swagger UI:** http://localhost:8080/swagger-ui.html  
**Actuator Health:** http://localhost:8080/actuator/health

#### **Frontend** (Fase 8)

```bash
cd frontend

# Instalar dependencias
npm install

# Arrancar desarrollo
npm run dev
```

**Frontend disponible en:** http://localhost:5173

---

### **Opción 2: Docker Compose (Recomendado)**

```bash
# Arrancar todos los servicios
docker compose up

# O en modo detached
docker compose up -d

# Ver logs
docker compose logs -f backend

# Detener servicios
docker compose down
```

**Servicios:**
- 🌐 **Frontend:** http://localhost:80
- 🚀 **Backend API:** http://localhost:8080
- 📊 **Swagger UI:** http://localhost:8080/swagger-ui.html
- 🗄️ **MySQL:** localhost:3306 (usuario: `renewsim`, contraseña: `renewsim123`)

---

## 🧪 Testing

### **Estrategia de Testing**

```
         /\
        /  \       10% E2E (RestAssured + Playwright)
       /____\
      /      \     30% Integration (Testcontainers)
     /________\
    /          \   60% Unit (jUnit 5 + Mockito + jqwik)
   /____________\
```

**Objetivo de cobertura:** ≥70% (enforcement con JaCoCo)

### **Ejecutar Tests**

```bash
cd backend

# Tests unitarios
./mvnw test

# Tests de integración (requiere Docker)
./mvnw verify -Dspring.profiles.active=testcontainers

# Verificar cobertura ≥70%
./mvnw jacoco:check

# Generar reporte HTML
./mvnw jacoco:report
# Reporte disponible en: target/site/jacoco/index.html
```

### **Property-Based Testing (jqwik)**

18 propiedades formales para validar invariantes del motor de simulación:

- ✅ **Prop 9:** Energía generada siempre > 0
- ✅ **Prop 10:** Monotonicidad (duplicar capacidad → duplicar energía)
- ✅ **Prop 11:** CO₂ siempre > 0
- ✅ **Prop 12:** Payback siempre > 0
- ✅ **Prop 13:** IRR round-trip con NPV ≈ 0
- ✅ **Prop 14:** NPV positivo para proyectos rentables
- ✅ **Prop 15:** Validación de parámetros (capacityKw ∈ [1, 10000])
- ✅ **Prop 16:** Máquina de estados (transiciones inválidas lanzan excepción)

### **Tests de Integración (Testcontainers)**

```java
@SpringBootTest
@Testcontainers
class SimulationLifecycleIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
    
    @Test
    void shouldCreateSimulationAndPersistResults() {
        // Flujo: POST /simulations → GET → PUT → DELETE
    }
}
```

### **Tests E2E (RestAssured)**

```java
@Test
void fullUserJourney_fromRegistrationToSimulation() {
    // POST /register → POST /activate → POST /login/step1 
    // → POST /login/step2 → POST /simulations 
    // → GET /simulations/{id} → POST /logout
}
```

---

## 🚀 Estado del Desarrollo

### **Fases Completadas**
- [x] **Fase 0.1:** Especificación de Requisitos (24 RF + 8 RNF)
- [x] **Fase 0.2:** Diseño Arquitectónico Completo (10 documentos)
- [x] **Fase 0.3:** Plan de Implementación (8 fases, 123 tasks)

### **Fases en Progreso**
- [ ] **Fase 1:** Backend Foundation (0/10 tasks) - 3 días
- [ ] **Fase 2:** User & Auth Service (0/27 tasks) - 4 días
- [ ] **Fase 3:** Technology & Scenario (0/11 tasks) - 2 días
- [ ] **Fase 4:** Simulation Engine (0/14 tasks) - 5 días
- [ ] **Fase 5:** Simulation CRUD (0/13 tasks) - 3 días
- [ ] **Fase 6:** Advanced Features (0/10 tasks) - 2 días
- [ ] **Fase 7:** AI Service (0/17 tasks) - 3 días
- [ ] **Fase 8:** Frontend (0/21 tasks) - 5 días

**Progreso global:** 0% (0/123 tasks completadas)  
**Estimación:** 27 días (6 días de buffer hasta 15 abril 2026)

---

## 📖 Documentación

### **Especificación de Requisitos**
- [requirements.md](docs/requirements/requirements.md) - 24 requisitos funcionales + 8 no funcionales

### **Documentos de Diseño (10 archivos)**
1. [Visión Arquitectónica General](docs/design/01-architecture-overview.md) - Hexagonal + 6 ADRs
2. [Modelo de Dominio](docs/design/02-domain-model.md) - DDD táctico (Aggregates + VOs + Services)
3. [Diseño de Base de Datos](docs/design/03-database-design.md) - ERD + índices + migraciones Flyway V1-V8
4. [Diseño de API REST](docs/design/04-api-design.md) - 42 endpoints con contratos completos
5. [Diagramas de Secuencia](docs/design/05-sequence-diagrams.md) - 5 flujos principales (Mermaid)
6. [Arquitectura Frontend](docs/design/06-frontend-architecture.md) - React + TanStack Query + Zustand
7. [Patrones de Diseño](docs/design/07-design-patterns.md) - 13 patrones con ejemplos de código
8. [Estrategia de Testing](docs/design/08-testing-strategy.md) - Pirámide + PBT + ejemplos
9. [Diseño de Seguridad](docs/design/09-security-design.md) - 6 amenazas + mitigaciones + Spring Security
10. [Arquitectura de Despliegue](docs/design/10-deployment-architecture.md) - Dockerfiles + CI/CD + env vars

### **Plan de Implementación**
- [IMPLEMENTATION_PLAN.md](docs/implementation/IMPLEMENTATION_PLAN.md) - 8 fases detalladas con 123 tasks

---

## 🌐 API REST - Endpoints Principales

### **Autenticación (auth_service)**
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/auth/login/step1` | Validar email + password → enviar OTP | No |
| POST | `/api/v1/auth/login/step2` | Validar OTP → retornar JWT | No |
| POST | `/api/v1/auth/refresh` | Renovar access token | Refresh cookie |
| POST | `/api/v1/auth/logout` | Blacklist JTI + revocar refresh | JWT |

### **Usuarios (user_service)**
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/users/register` | Registro de usuario → email activación | No |
| POST | `/api/v1/users/activate` | Activar cuenta con token | No |
| GET | `/api/v1/users/me` | Perfil del usuario autenticado | JWT |
| PUT | `/api/v1/users/me` | Actualizar nombre, teléfono | JWT |

### **Tecnologías (technology_service)**
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/technologies` | Listar tecnologías (paginado, cache 10 min) | JWT |
| GET | `/api/v1/technologies/{id}` | Detalle de tecnología | JWT |
| POST | `/api/v1/technologies` | Crear tecnología (solo ADMIN) | JWT |

### **Simulaciones (simulation_service)**
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/simulations` | Crear simulación → calcular → COMPLETED | JWT |
| GET | `/api/v1/simulations/{id}` | Detalle (solo owner o ADMIN) | JWT |
| GET | `/api/v1/simulations/my-simulations` | Mis simulaciones (paginado) | JWT |
| PUT | `/api/v1/simulations/{id}` | Actualizar (solo DRAFT) | JWT |
| DELETE | `/api/v1/simulations/{id}` | Archivar (soft delete) | JWT |
| POST | `/api/v1/simulations/compare` | Comparar 2-5 simulaciones | JWT |
| POST | `/api/v1/simulations/{id}/share` | Generar token compartición | JWT |
| GET | `/api/v1/shared/{token}` | Vista pública simulación | No |
| GET | `/api/v1/dashboard` | Métricas agregadas + mapa | JWT |

### **Inteligencia Artificial (ai_service)**
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/ai/suggest-configuration` | Sugerencias IA según ubicación | JWT |
| POST | `/api/v1/ai/predict-performance/{id}` | Proyecciones a N años | JWT |
| POST | `/api/v1/ai/generate-report/{id}` | Reporte narrativo multiidioma | JWT |
| POST | `/api/v1/ai/chat` | Chatbot conversacional | JWT |

**Total:** 42 endpoints documentados en [04-api-design.md](docs/design/04-api-design.md)

---

## 🔒 Seguridad

### **Autenticación 2FA**
1. **Paso 1:** POST `/auth/login/step1` (email + password) → valida credenciales → genera OTP 6 dígitos → envía email
2. **Paso 2:** POST `/auth/login/step2` (email + OTP) → valida OTP (5 min) → retorna JWT access + refresh

### **JWT Tokens**
- **Access Token:** 1 hora, en header `Authorization: Bearer {token}`, claims: sub, jti, roles
- **Refresh Token:** 7 días, en cookie `HttpOnly; Secure; SameSite=Strict`, rotación en cada refresh

### **Rate Limiting (Caffeine)**
- 5 intentos de login fallidos → bloqueo 15 minutos
- 3 intentos de OTP fallidos → bloqueo 15 minutos

### **Token Blacklist (JTI)**
- Revocación inmediata en `/logout`
- Verificación en `JwtAuthenticationFilter` con cache Caffeine

### **Headers de Seguridad**
```yaml
HSTS: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Content-Security-Policy: default-src 'self'
```

### **Validación de API Keys**
```java
@PostConstruct
void validateApiKey() {
    if (apiKey == null || apiKey.isEmpty() || apiKey.equals("sk-...")) {
        throw new IllegalStateException("API key is not configured");
    }
}
```

---

## 🐳 CI/CD con GitHub Actions

### **Pipeline de CI**

```yaml
name: CI - Backend Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_DATABASE: renewsim_test
          MYSQL_ROOT_PASSWORD: test
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests
        run: ./mvnw clean test
      - name: Check coverage ≥70%
        run: ./mvnw jacoco:check
```

**Status:** [![CI](https://github.com/LannyRivero/renewsim-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/LannyRivero/renewsim-platform/actions/workflows/ci.yml)

---

## 📊 Fórmulas del Motor de Simulación

### **Generación de Energía**
```
energyGenerated = capacity × efficiency × 8760 × climateFactor
```

**Climate Factors:**
- SOLAR: `avgSolarIrradiation × 365 / 24`
- WIND: `(avgWindSpeed / 12)³ × 0.593`
- HYDRO: `0.85` (constante)

### **Retorno de Inversión (ROI)**
```
ROI = ((totalRevenue - initialInvestment) / initialInvestment) × 100
```

### **Periodo de Retorno (Payback)**
```
payback = initialInvestment / annualSavings
```

### **Reducción de CO₂**
```
co2Reduction = energyGenerated × 0.5 / 1000  (toneladas/año)
```

### **Valor Actual Neto (NPV)**
```
NPV = Σ[cashFlow_t / (1 + discountRate)^t] - initialInvestment
```

### **Tasa Interna de Retorno (IRR)**
```
IRR se calcula iterativamente con Newton-Raphson donde NPV(IRR) = 0
```

---

## 👨‍💻 Autor

**Lanny Rivero Canino**  

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Lanny%20Rivero-blue?logo=linkedin)](https://www.linkedin.com/in/lannyriverocanino/)  
[![GitHub](https://img.shields.io/badge/GitHub-YOUR_USERNAME-black?logo=github)](https://github.com/YOUR_USERNAME)

---

## 📄 Licencia

Este proyecto está bajo la licencia [MIT](LICENSE).
---

## 🙏 Agradecimientos

- **OpenAI** y **Anthropic** por las APIs de LLM
- **Spring Boot Team** por el framework
- **Testcontainers** por facilitar testing de integración
- **Comunidad Open Source** por las librerías utilizadas

---

**⭐ Si este proyecto te resulta útil para tu investigación o aprendizaje, considera darle una estrella en GitHub**
---

