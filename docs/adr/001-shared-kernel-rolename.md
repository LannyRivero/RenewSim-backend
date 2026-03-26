# ADR 001: Extract RoleName to Shared Kernel

## Status
✅ Accepted (2026-03-26)

## Context
RoleName enum used by 15+ files across auth, user, role services.
Original location (role_service.domain.model) created implicit coupling.

## Decision
Extract to `shared/domain/vo/RoleName.java` as shared kernel VO.

## Consequences
**Positive:**
- Explicit shared contract
- Reduced cross-BC coupling
- Clear ownership (shared kernel)

**Negative:**
- Breaking change (package relocation)
- Requires synchronized deployment

**Neutral:**
- RoleName immutable (low evolution risk)

## Validation
- ✅ 358 files compiled successfully
- ✅ 286/358 tests passing (72 pre-existing failures unrelated)
- ✅ Zero import errors

## References
- Commit: ac617fe
- MIGRATION_MAP.md
- Test debt: TECH_DEBT_001
