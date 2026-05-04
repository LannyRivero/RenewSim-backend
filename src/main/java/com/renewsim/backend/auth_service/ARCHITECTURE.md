# Auth Service - Architecture Decision Record

## Aggregate Root Decision

**Date**: 2026-04-24  
**Status**: Decided  
**Decision**: No explicit Aggregate Root for auth_service

---

## Context

The auth_service is a **technical authentication service** responsible for:
- Email verification (`EmailVerificationToken`)
- JWT refresh token rotation (`RefreshToken`)
- Login/logout flows

These entities have independent lifecycles and do not share complex invariants that would require an Aggregate Root to enforce consistency.

These entities have independent lifecycles and do not share complex invariants that would require an Aggregate Root to enforce consistency.

---

## Decision

We will **NOT** introduce an explicit Aggregate Root (e.g., `AuthSession`) for the following reasons:

### 1. Technical Service Nature
auth_service is a **technical/infrastructure service**, not a domain-rich business service. Its entities are technical artifacts (tokens) rather than business concepts with complex invariants.

### 2. Independent Entities
- `EmailVerificationToken`: Short-lived (48h), used during registration
- `RefreshToken`: Long-lived (7 days), used for JWT rotation
- N/A (no 2FA - email verification only)

These entities do not form a natural cluster with shared invariants.

### 3. Use Cases as Orchestrators
The application layer use cases (`RegisterUserService`, `VerifyEmailUseCase`, `RefreshTokenService`, etc.) correctly orchestrate interactions between these entities and external ports. This is the appropriate place for coordination logic in a technical service.

### 4. KISS Principle
Forcing an artificial Aggregate Root (e.g., `AuthSession`) would:
- Add unnecessary complexity
- Create artificial relationships between unrelated entities
- Violate the KISS (Keep It Simple, Stupid) principle

---

## Current Design (Accepted)

### Domain Layer
- `EmailVerificationToken` - Independent entity for email verification
- `RefreshToken` - Independent entity for refresh tokens

### Application Layer
- Use cases orchestrate entity interactions
- Ports define contracts for infrastructure
- No domain service acts as Aggregate Root (by design choice)

### Compliance
✅ Hexagonal Architecture: Strict layer separation  
✅ DDD: Entities are in domain layer, no framework dependencies  
✅ SOLID: Single Responsibility per entity and use case  
✅ KISS: No over-engineering

---

## Consequences

**Positive**:
- Simpler codebase
- Easier to understand and maintain
- Each entity has clear, single responsibility
- Use cases remain the orchestration point (appropriate for technical services)

**Negative**:
- None significant. This is the appropriate design for a technical auth service.

---

## Alternatives Considered

### Alternative: `AuthSession` as Aggregate Root
**Rejected because**:
- Artificial grouping of unrelated entities
- No 2FA in current design (email verification only)
- RefreshToken has different lifecycle than verification token
- Would add complexity without business justification

---

## References

- DDD: Aggregate Pattern (Eric Evans)
- Hexagonal Architecture (Alistair Cockburn)
- RenewSim Architecture Guidelines (AGENTS.md)