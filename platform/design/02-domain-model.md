# Modelo de Dominio — RenewSim

## Aggregate Root: Simulation

La `Simulation` es el aggregate root del bounded context `simulation_service`. Encapsula todos los parámetros de entrada, resultados calculados y el estado del ciclo de vida.

### Invariantes del Aggregate

- `capacityKw` debe estar en el rango [1, 10.000]
- `initialInvestment` debe ser > 0
- `latitude` debe estar en [-90, 90], `longitude` en [-180, 180]
- Las transiciones de estado solo siguen: `DRAFT → COMPLETED → ARCHIVED`
- Los resultados calculados solo son válidos cuando `status = COMPLETED`

### Máquina de Estados

```mermaid
stateDiagram-v2
    [*] --> DRAFT : create / from-scenario
    DRAFT --> COMPLETED : calculate()
    DRAFT --> ARCHIVED : archive() / soft-delete
    COMPLETED --> ARCHIVED : archive() / soft-delete
    COMPLETED --> DRAFT : [PROHIBIDO — HTTP 409]
    ARCHIVED --> DRAFT : [PROHIBIDO — HTTP 409]
    ARCHIVED --> COMPLETED : [PROHIBIDO — HTTP 409]
```

---

## Diagrama de Clases del Dominio

```mermaid
classDiagram
    class Simulation {
        +Long id
        +Long userId
        +Long technologyId
        +String name
        +SimulationStatus status
        +Location location
        +Money initialInvestment
        +double capacityKw
        +double electricityTariff
        +ClimateData climateData
        +EnergyData energyData
        +double roiPercentage
        +double paybackYears
        +double co2ReductionAnnual
        +double npvValue
        +double irrPercentage
        +LocalDateTime createdAt
        +int version
        +completeCalculation(SimulationResults) void
        +archive() void
        +clone(Long userId) Simulation
        +transitionTo(SimulationStatus) void
    }

    class SimulationStatus {
        <<enumeration>>
        DRAFT
        COMPLETED
        ARCHIVED
    }

    class Location {
        <<value object>>
        +double latitude
        +double longitude
        +validate() void
        +distanceTo(Location) double
    }

    class Money {
        <<value object>>
        +BigDecimal amount
        +String currency
        +isPositive() boolean
        +add(Money) Money
    }

    class EnergyData {
        <<value object>>
        +double kwhPerYear
        +double[] monthlyBreakdown
    }

    class ClimateData {
        <<value object>>
        +double avgSolarIrradiation
        +double avgWindSpeed
        +double avgTemperature
        +double climateFactor
    }

    class EnergyType {
        <<enumeration>>
        SOLAR
        WIND
        HYDRO
        BIOMASS
        GEOTHERMAL
    }

    class Technology {
        +Long id
        +String name
        +EnergyType energyType
        +double efficiency
        +double baseCostPerKw
        +int lifespanYears
        +double maintenanceCostPct
        +boolean isActive
    }

    class Scenario {
        +Long id
        +String name
        +String description
        +Long technologyId
        +double defaultCapacityKw
        +Money defaultInvestment
        +double defaultTariff
        +ClimateData climateProfile
        +boolean isActive
    }

    class User {
        +Long id
        +String email
        +String passwordHash
        +String fullName
        +UserStatus status
        +Set~Role~ roles
        +LocalDateTime createdAt
        +LocalDateTime activatedAt
    }

    class UserStatus {
        <<enumeration>>
        ACTIVE
        INACTIVE
        SUSPENDED
    }

    class Role {
        +Long id
        +String name
        +String description
    }

    class OtpCode {
        +Long id
        +Long userId
        +String codeHash
        +LocalDateTime expiresAt
        +boolean used
        +isExpired() boolean
        +isValid() boolean
    }

    class RefreshToken {
        +Long id
        +Long userId
        +String tokenHash
        +LocalDateTime expiresAt
        +boolean revoked
        +isValid() boolean
    }

    class ChatSession {
        +Long id
        +Long userId
        +String sessionId
        +List~ChatMessage~ messages
        +LocalDateTime lastActive
        +addMessage(role, content) void
    }

    class ChatMessage {
        +Long id
        +MessageRole role
        +String content
        +int tokensUsed
        +LocalDateTime createdAt
    }

    class MessageRole {
        <<enumeration>>
        USER
        ASSISTANT
        SYSTEM
    }

    Simulation --> SimulationStatus
    Simulation --> Location
    Simulation --> Money
    Simulation --> EnergyData
    Simulation --> ClimateData
    Simulation --> Technology
    Technology --> EnergyType
    Scenario --> Technology
    User --> UserStatus
    User --> Role
    User --> OtpCode
    User --> RefreshToken
    ChatSession --> User
    ChatSession --> ChatMessage
    ChatMessage --> MessageRole
```

---

## Value Objects

### Location

```java
public record Location(double latitude, double longitude) {
    public Location {
        if (latitude < -90 || latitude > 90)
            throw new InvalidLocationException("Latitude must be between -90 and 90");
        if (longitude < -180 || longitude > 180)
            throw new InvalidLocationException("Longitude must be between -180 and 180");
    }
}
```

### Money

```java
public record Money(BigDecimal amount, String currency) {
    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidMoneyException("Amount cannot be negative");
    }
    public boolean isPositive() { return amount.compareTo(BigDecimal.ZERO) > 0; }
}
```

### ClimateData

```java
public record ClimateData(
    double avgSolarIrradiation,  // kWh/m²/día
    double avgWindSpeed,          // m/s
    double avgTemperature         // °C
) {}
```

---

## Puertos del Dominio

```java
// Puerto de salida — Repositorio de simulaciones
public interface SimulationRepository {
    Simulation save(Simulation simulation);
    Optional<Simulation> findById(Long id);
    List<Simulation> findByUserIdAndStatusNot(Long userId, SimulationStatus status, Pageable pageable);
    void delete(Long id);
}

// Puerto de entrada — Caso de uso de simulación
public interface CreateSimulationUseCase {
    SimulationCreationResultDTO execute(CreateSimulationCommand command);
}

// Puerto de salida — Proveedor LLM
public interface LLMProviderPort {
    AIResponse complete(AIRequest request);
}

// Puerto de salida — Servicio de email
public interface EmailPort {
    void sendOtpEmail(String to, String otpCode);
    void sendActivationEmail(String to, String activationToken);
}
```
