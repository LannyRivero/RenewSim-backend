# ADR-005: Enum Persistence Strategy (VARCHAR vs Native ENUM)

**Status:** Accepted  
**Date:** 2026-04-02  
**Context:** Task 1.2 Phase B - Technology Service Hexagonal Migration  
**Deciders:** Lanny (Developer), Claude (Technical Mentor)

---

## Context

Role names and other domain enums (e.g., `RoleName`, `TechnologyType`) require persistence in MySQL.
Hibernate detected a schema mismatch during validation:
- **Database schema (Flyway):** `roles.name` defined as `VARCHAR(50)`
- **JPA mapping (Hibernate):** Auto-inferred as MySQL native `ENUM` type
- **Error:** Application startup blocked by schema validation failure

Two persistence strategies were evaluated:

### Option A: VARCHAR with @Enumerated(STRING)
- Store enum values as variable-length strings (e.g., `'USER'`, `'ADMIN'`)
- Hibernate maps Java enum to `VARCHAR` column explicitly
- No database-level type validation

### Option B: MySQL Native ENUM
- Use MySQL's native `ENUM('USER','ADMIN','ANALYST')` column type
- Database enforces valid values at storage layer
- Requires schema migration for each new enum value

---

## Decision

**Use VARCHAR(50) with explicit `@Enumerated(EnumType.STRING)` mapping.**

All domain enums persisted to relational storage will use this pattern:
```java
@Enumerated(EnumType.STRING)
@Column(name = "column_name", nullable = false, length = 50)
private EnumType fieldName;
```

Flyway migrations define columns as:
```sql
column_name VARCHAR(50) NOT NULL
```

---

## Rationale

### Why VARCHAR wins for this project:

#### 1. **Evolutionary Flexibility (P0 - Critical)**
- Adding new enum values requires **only Java code changes** (deploy-time)
- No `ALTER TABLE` operations → zero downtime
- Example: Adding `RoleName.PREMIUM_USER`:
```java
  // Change enum → deploy → done
  public enum RoleName { USER, ADMIN, ANALYST, PREMIUM_USER }
```
- With native ENUM: Requires migration + table lock + potential downtime

#### 2. **Database Portability (P1 - Important)**
- Works identically across MySQL, PostgreSQL (with CHECK constraint), H2 (tests)
- Native ENUM syntax varies significantly:
  - MySQL: `ENUM('A','B')`
  - PostgreSQL: `CREATE TYPE + custom type`
  - H2: No native ENUM support
- Reduces vendor lock-in

#### 3. **Hexagonal Architecture Alignment (P1 - Important)**
- Domain layer defines enums as pure Java types
- Infrastructure layer stores strings (implementation detail)
- No coupling between domain invariants and database-specific types
- **Separation of concerns:** validation in application layer, persistence in infrastructure

#### 4. **Validation Remains Robust (P2 - Acceptable Trade-off)**
- Java enum constrains possible values at compile-time
- Hibernate `@Enumerated` maps safely
- Bean Validation (`@NotNull`) provides runtime safety
- Trade-off accepted: No DB-level CHECK constraint (not needed given Java enforcement)

#### 5. **Project Context (Academic TFM)**
- Focus: Architecture, patterns, maintainability
- Not focus: Extreme storage optimization (roles table: <100 rows expected)
- Storage overhead: ~5 bytes/row vs 1 byte with ENUM (negligible)

---

## Consequences

### Positive
✅ New roles deployable without schema changes  
✅ Rollback simple: revert code, no DB rollback needed  
✅ Tests run identically in H2/MySQL (no mocking enum behavior)  
✅ Future DB migration (MySQL → PostgreSQL) simplified  
✅ Aligns with DDD tactical patterns (domain enum = value object)

### Negative
⚠️ No database-level validation (acceptable: Java layer enforces)  
⚠️ ~4 bytes overhead per row (negligible at expected scale <10K roles total)  
⚠️ Cannot query DB directly with autocomplete for valid values (minor DX impact)

### Neutral
🔄 If future switch to native ENUM needed: Medium effort (data migration + mapping update)  
🔄 Performance impact: None (VARCHAR indexed same as ENUM for small value sets)

---

## Compliance with Production Standards

| Criterion | Evaluation |
|-----------|-----------|
| **Evolvability** | ✅ Excellent - zero-migration enum additions |
| **Maintainability** | ✅ Strong - consistent pattern across all enums |
| **Testability** | ✅ Excellent - H2 compatibility maintained |
| **Security** | ✅ Adequate - Java enum prevents injection |
| **Performance** | ✅ Acceptable - overhead negligible at scale |
| **Simplicity** | ✅ High - standard JPA pattern, no custom types |

---

## Implementation Notes

### All domain enums follow this template:

**Java Entity:**
```java
@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 50)
private SimulationStatus status;
```

**Flyway Migration:**
```sql
status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
```

### Affected bounded contexts:
- `role_service`: `RoleName` (`USER`, `ADMIN`, `ANALYST`)
- `technology_service`: Potential future enums (e.g., `TechnologyStatus`)
- `simulation_service`: `SimulationStatus` (`PENDING`, `RUNNING`, `COMPLETED`, `FAILED`)
- `ai_service`: `MessageRole` (`USER`, `ASSISTANT`, `SYSTEM`)

---

## Alternatives Considered

### Native MySQL ENUM
**Rejected** because:
- Requires `ALTER TABLE` for each new value (downtime risk)
- Poor portability (PostgreSQL, H2 need different approaches)
- Couples domain to infrastructure (violates hexagonal principles)
- Over-engineering for project scale (roles table <100 rows)

### @Enumerated(EnumType.ORDINAL)
**Rejected** because:
- Stores integer indexes (0, 1, 2...) → unreadable in DB
- Fragile: reordering enum breaks data integrity
- Not production-grade (violates "code must be understandable")

---

## Related Decisions
- **ADR-004:** Test Failure Categorization (references enum validation in tech debt)
- **Task 1.2 Phase A:** `RoleName` extraction as shared domain value object

---

## Revision History
- **2026-04-02:** Initial decision (VARCHAR strategy) - Lanny