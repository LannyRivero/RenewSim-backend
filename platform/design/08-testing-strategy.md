# Estrategia de Testing — RenewSim

## Enfoque Dual: Tests Unitarios + Property-Based Testing

Los tests unitarios verifican ejemplos concretos y casos de borde. Los tests de propiedad verifican invariantes universales sobre rangos amplios de entradas generadas aleatoriamente.

| Tipo | Librería Backend | Librería Frontend | Cuándo usar |
|------|-----------------|-------------------|-------------|
| Unitario | JUnit 5 + Mockito | Vitest + Testing Library | Comportamiento concreto, casos de borde |
| Propiedad (PBT) | jqwik | fast-check | Invariantes del motor, round-trips |
| Integración | Testcontainers + MySQL | MSW (Mock Service Worker) | Repositorios, flujos completos |
| E2E | RestAssured | Playwright | Flujos críticos de usuario |
| Seguridad | MockMvc | — | Autorización, rate limiting |

**Cobertura mínima:** ≥70% de líneas por bounded context (JaCoCo enforcement en CI).

---

## Propiedades de Corrección (Property-Based Tests)

### Propiedad 1: Energía generada es siempre positiva

```java
// Feature: renewsim-platform, Property 9
@Property(tries = 200)
void energyGeneratedIsAlwaysPositive(
    @ForAll @DoubleRange(min = 1.0, max = 10000.0) double capacityKw,
    @ForAll @DoubleRange(min = 0.01, max = 1.0) double efficiency,
    @ForAll @DoubleRange(min = 0.1, max = 10.0) double climateFactor
) {
    SimulationInput input = SimulationInput.builder()
        .capacityKw(capacityKw)
        .efficiency(efficiency)
        .hoursPerYear(8760)
        .climateFactor(climateFactor)
        .build();

    SimulationResults results = engine.calculate(input);

    assertThat(results.energyGenerated()).isGreaterThan(0.0);
}
```

### Propiedad 2: Monotonicidad de energía respecto a capacidad

```java
// Feature: renewsim-platform, Property 10
@Property(tries = 100)
void energyIsMonotonicallyProportionalToCapacity(
    @ForAll @DoubleRange(min = 1.0, max = 5000.0) double capacity1,
    @ForAll @DoubleRange(min = 0.01, max = 1.0) double efficiency,
    @ForAll @DoubleRange(min = 0.1, max = 10.0) double climateFactor
) {
    double capacity2 = capacity1 * 2.0;

    SimulationInput input1 = buildInput(capacity1, efficiency, climateFactor);
    SimulationInput input2 = buildInput(capacity2, efficiency, climateFactor);

    double energy1 = engine.calculate(input1).energyGenerated();
    double energy2 = engine.calculate(input2).energyGenerated();

    // energy2 debe ser exactamente el doble de energy1 (±0.1% tolerancia)
    assertThat(energy2 / energy1).isCloseTo(2.0, within(0.001));
}
```

### Propiedad 3: TIR es round-trip con VAN

```java
// Feature: renewsim-platform, Property 13
@Property(tries = 100)
void irrIsRoundTripWithNpv(
    @ForAll @DoubleRange(min = 1000.0, max = 100000.0) double initialInvestment,
    @ForAll @DoubleRange(min = 100.0, max = 20000.0) double annualSavings,
    @ForAll @IntRange(min = 10, max = 30) int lifespanYears
) {
    Assume.that(annualSavings * lifespanYears > initialInvestment); // Proyecto viable

    double[] cashFlows = buildCashFlows(annualSavings, lifespanYears);
    double irr = irrCalculator.calculate(cashFlows, initialInvestment);
    double npvAtIrr = npvCalculator.calculate(cashFlows, irr, initialInvestment);

    // NPV calculado con la TIR debe ser ≈ 0
    assertThat(npvAtIrr).isCloseTo(0.0, within(0.01));
}
```

### Propiedad 4: Serialización SimulationRequestDTO es round-trip

```typescript
// Feature: renewsim-platform, Property 17
test('SimulationRequestDTO serialization round-trip', () => {
  fc.assert(
    fc.property(
      fc.record({
        name: fc.string({ minLength: 1, maxLength: 255 }),
        capacityKw: fc.double({ min: 1, max: 10000, noNaN: true }),
        initialInvestment: fc.double({ min: 0.01, max: 1e9, noNaN: true }),
        latitude: fc.double({ min: -90, max: 90, noNaN: true }),
        longitude: fc.double({ min: -180, max: 180, noNaN: true }),
        electricityTariff: fc.double({ min: 0.01, max: 2.0, noNaN: true }),
      }),
      (dto) => {
        const serialized = JSON.stringify(dto);
        const deserialized = JSON.parse(serialized);
        expect(deserialized.name).toBe(dto.name);
        expect(deserialized.capacityKw).toBeCloseTo(dto.capacityKw, 8);
        expect(deserialized.latitude).toBeCloseTo(dto.latitude, 8);
      }
    ),
    { numRuns: 100 }
  );
});
```

---

## Tests de Integración con Testcontainers

```java
@SpringBootTest
@Testcontainers
class SimulationRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("renewsim_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private SimulationRepository simulationRepository;

    @Test
    void shouldSaveAndRetrieveSimulation() {
        Simulation simulation = Simulation.builder()
            .userId(1L)
            .technologyId(1L)
            .name("Test Simulation")
            .status(SimulationStatus.DRAFT)
            .capacityKw(5.0)
            .location(new Location(40.4168, -3.7038))
            .initialInvestment(new Money(BigDecimal.valueOf(7500), "USD"))
            .electricityTariff(0.15)
            .build();

        Simulation saved = simulationRepository.save(simulation);
        Optional<Simulation> retrieved = simulationRepository.findById(saved.getId());

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Test Simulation");
        assertThat(retrieved.get().getStatus()).isEqualTo(SimulationStatus.DRAFT);
        assertThat(retrieved.get().getLocation().latitude()).isEqualTo(40.4168);
    }
}
```

---

## Tests E2E — Flujo Completo con RestAssured

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class SimulationE2ETest {

    @LocalServerPort
    private int port;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    private String accessToken;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost:" + port;

        // 1. Registro
        given().contentType("application/json")
            .body("""
                {
                  "email": "e2e@test.com",
                  "password": "SecurePass123!",
                  "fullName": "E2E Test User"
                }
                """)
        .when().post("/api/v1/users/register")
        .then().statusCode(201);

        // 2. Activación (obtener token de BD directamente en test)
        String activationToken = getActivationTokenFromDB("e2e@test.com");
        given().contentType("application/json")
            .body("""{ "token": "%s" }""".formatted(activationToken))
        .when().post("/api/v1/users/activate")
        .then().statusCode(200);

        // 3. Login step 1
        given().contentType("application/json")
            .body("""
                { "email": "e2e@test.com", "password": "SecurePass123!" }
                """)
        .when().post("/api/v1/auth/login/step1")
        .then().statusCode(200);

        // 4. Login step 2 (obtener OTP de BD directamente en test)
        String otpCode = getOtpCodeFromDB("e2e@test.com");
        accessToken = given().contentType("application/json")
            .body("""
                { "email": "e2e@test.com", "otpCode": "%s" }
                """.formatted(otpCode))
        .when().post("/api/v1/auth/login/step2")
        .then().statusCode(200)
        .extract().path("accessToken");
    }

    @Test
    void shouldCreateSimulationAndGetDetail() {
        int simulationId = given()
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
            .body("""
                {
                  "name": "E2E Solar Test",
                  "technologyId": 1,
                  "location": { "latitude": 40.4168, "longitude": -3.7038 },
                  "capacityKw": 5.0,
                  "initialInvestment": 7500.0,
                  "electricityTariff": 0.15,
                  "climateData": {
                    "avgSolarIrradiation": 5.5,
                    "avgWindSpeed": 3.2,
                    "avgTemperature": 22.0
                  }
                }
                """)
        .when().post("/api/v1/simulations")
        .then()
            .statusCode(201)
            .body("status", equalTo("COMPLETED"))
            .body("energyGeneratedAnnual", greaterThan(0f))
            .body("roiPercentage", greaterThan(0f))
        .extract().path("id");

        // Verificar detalle
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when().get("/api/v1/simulations/" + simulationId)
        .then()
            .statusCode(200)
            .body("name", equalTo("E2E Solar Test"))
            .body("status", equalTo("COMPLETED"));
    }
}
```

---

## Tests de Seguridad

```java
@WebMvcTest(SimulationController.class)
class SimulationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimulationApplicationService simulationService;

    @Test
    void shouldReturn401WhenNoJwtProvided() throws Exception {
        mockMvc.perform(get("/api/v1/simulations/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenUserAccessesOtherUserSimulation() throws Exception {
        when(simulationService.get(any()))
            .thenThrow(new AccessDeniedException("Simulation does not belong to user"));

        mockMvc.perform(get("/api/v1/simulations/999")
                .header("Authorization", "Bearer " + generateJwt(1L, List.of("USER"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void shouldReturn403WhenUserTriesToCreateTechnology() throws Exception {
        mockMvc.perform(post("/api/v1/technologies")
                .header("Authorization", "Bearer " + generateJwt(1L, List.of("USER")))
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn409WhenUpdatingCompletedSimulation() throws Exception {
        when(simulationService.update(any()))
            .thenThrow(new InvalidStateTransitionException(
                "Cannot modify a COMPLETED simulation"
            ));

        mockMvc.perform(put("/api/v1/simulations/42")
                .header("Authorization", "Bearer " + generateJwt(1L, List.of("USER")))
                .contentType("application/json")
                .content("""{ "name": "Updated" }"""))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("INVALID_STATE_TRANSITION"));
    }
}
```

---

## Cobertura por Bounded Context

| Bounded Context | Tests Unitarios | Tests PBT | Tests Integración | Cobertura Objetivo |
|----------------|----------------|-----------|-------------------|-------------------|
| auth_service | Flujo 2FA, JWT, blacklist | Props 5, 6, 7, 8 | Testcontainers MySQL | ≥70% |
| user_service | Registro, activación, perfil | Props 2, 3, 4 | Testcontainers MySQL | ≥70% |
| simulation_service | Cálculos, ciclo de vida | Props 9–16 | Testcontainers MySQL | ≥80% (core) |
| technology_service | CRUD, caché | — | WireMock | ≥70% |
| ai_service | Prompts, fallback | — | WireMock (LLM mock) | ≥70% |

### Configuración JaCoCo (pom.xml)

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```
